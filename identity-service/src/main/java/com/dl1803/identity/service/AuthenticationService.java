package com.dl1803.identity.service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.dl1803.event.dto.NotificationEvent;
import com.dl1803.identity.dto.request.*;
import com.dl1803.identity.dto.response.AuthenticationResponse;
import com.dl1803.identity.dto.response.IntrospectResponse;
import com.dl1803.identity.entity.InvalidatedToken;
import com.dl1803.identity.entity.Role;
import com.dl1803.identity.entity.User;
import com.dl1803.identity.enums.AuthProvider;
import com.dl1803.identity.exception.AppException;
import com.dl1803.identity.exception.ErrorCode;
import com.dl1803.identity.repository.InvalidatedRepository;
import com.dl1803.identity.repository.RoleRepository;
import com.dl1803.identity.repository.UserRepository;
import com.dl1803.identity.repository.httpclient.ProfileClient;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;

    PasswordEncoder passwordEncoder;

    InvalidatedRepository invalidatedRepository;

    @Value("${jwt.signerKey}")
    @NonFinal
    protected String SIGNER_KEY;

    @Value("${jwt.valid-duration}")
    @NonFinal
    protected long VALID_DURATION;

    @Value("${jwt.refreshable-duration}")
    @NonFinal
    protected long REFRESHABLE_DURATION;

    private final SecureRandom rd = new SecureRandom();

    KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${google.client-id}")
    @NonFinal
    String googleClientId;

    ProfileClient profileClient;

    RoleRepository roleRepository;

    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        var token = request.getToken();

        boolean isValid = true;
        try {
            verifyToken(token);
        } catch (AppException e) {
            isValid = false;
        }

        return IntrospectResponse.builder().valid(isValid).build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Signerkey: {}", SIGNER_KEY);
        var user = userRepository
                .findByUsername(request.getUsername())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIALS));

        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }

        // Kh cho phép user login bằng form khi đã login bằng GG (chỉ cho một chiều)
        if (AuthProvider.GOOGLE.equals(user.getAuthProvider()) && user.getPassword() == null) {
            throw new AppException(ErrorCode.ACCOUNT_LINKED_TO_DIFFERENT_PROVIDER);
        }

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!authenticated) throw new AppException(ErrorCode.INVALID_CREDENTIALS);

        var accessToken = generateAccessToken(user);
        var refreshToken = generateRefreshToken(user);

        Date expiry = new Date(java.time.Instant.now()
                .plus(VALID_DURATION, java.time.temporal.ChronoUnit.SECONDS)
                .toEpochMilli());
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiryTime(expiry)
                .build();
    }

    public void logout(LogoutRequest request) throws ParseException, JOSEException {
        // thu hồi accessToken
        try {
            var signToken = verifyToken(request.getAccessToken());
            String jit = signToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken =
                    InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

            invalidatedRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Access Token already expired or invalid");
        }

        // thu hồi refreshToken
        try {
            if (request.getRefreshToken() != null && !request.getRefreshToken().isEmpty()) {
                var signRefreshToken = verifyToken(request.getRefreshToken());
                String refreshJit = signRefreshToken.getJWTClaimsSet().getJWTID();
                Date refreshExpiryTime = signRefreshToken.getJWTClaimsSet().getExpirationTime();

                InvalidatedToken invalidatedRefreshToken = InvalidatedToken.builder()
                        .id(refreshJit)
                        .expiryTime(refreshExpiryTime)
                        .build();

                invalidatedRepository.save(invalidatedRefreshToken);
            }
        } catch (AppException e) {
            log.info("Refresh Token already expired or invalid");
        }
    }

    public AuthenticationResponse refreshToken(RefreshRequest request) throws ParseException, JOSEException {
        var signJWT = verifyToken(request.getRefreshToken());

        // check type xem có thực sự là refresh token hay không
        String type = signJWT.getJWTClaimsSet().getStringClaim("type");
        if (!"REFRESH".equals(type)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // Invalidate (Thu hồi) Refresh Token cũ để chống xài lại
        var jit = signJWT.getJWTClaimsSet().getJWTID();
        var expiryTime = signJWT.getJWTClaimsSet().getExpirationTime();

        InvalidatedToken invalidatedToken =
                InvalidatedToken.builder().id(jit).expiryTime(expiryTime).build();

        invalidatedRepository.save(invalidatedToken);

        // tìm user
        var username = signJWT.getJWTClaimsSet().getSubject();

        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.TOKEN_EXPIRED));

        if (!user.isEmailVerified()) {
            throw new AppException(ErrorCode.USER_NOT_VERIFIED);
        }

        // cấp lại token mới
        var accessToken = generateAccessToken(user);
        var refreshToken = generateRefreshToken(user);
        Date expiry = new Date(java.time.Instant.now()
                .plus(VALID_DURATION, java.time.temporal.ChronoUnit.SECONDS)
                .toEpochMilli());
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiryTime(expiry)
                .build();
    }

    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {

        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);

        if (!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        if (invalidatedRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.TOKEN_REVOKED);
        }

        // Check old token
        String userId = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.TOKEN_EXPIRED));

        if (user.getPasswordChangeAt() != null) {
            Date issueTime = signedJWT.getJWTClaimsSet().getIssueTime();
            LocalDateTime issueDateTime =
                    issueTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();

            if (issueDateTime.isBefore(user.getPasswordChangeAt())) {
                throw new AppException(ErrorCode.PASSWORD_CHANGED_RELOGIN);
            }
        }

        return signedJWT;
    }

    private String generateAccessToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("dl1803.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject()); // chuyển Data về dạng Json -> bỏ vào payload

        JWSObject jwsObject = new JWSObject(jwsHeader, payload); // cần truyền header và payload

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token", e);
            throw new RuntimeException(e);
        }
    }

    private String generateRefreshToken(User user) {
        JWSHeader jwsHeader = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getId())
                .issuer("dl1803.com")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now()
                        .plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS)
                        .toEpochMilli()))
                .jwtID(UUID.randomUUID().toString())
                // Phân biệt Token này chuyên dùng để Refresh
                .claim("type", "REFRESH")
                // KHÔNG GỌI hàm buildScope(user)
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(jwsHeader, payload);

        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token", e);
            throw new RuntimeException(e);
        }
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        }

        return stringJoiner.toString();
    }

    public void verifyEmail(VerifyEmailRequest request) {
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.isEmailVerified()) {
            throw new AppException(ErrorCode.USER_ALREADY_VERIFIED);
        }

        if (user.getOtpAttemptCount() >= 5) {
            throw new AppException(ErrorCode.TOO_MANY_OTP_ATTEMPTS);
        }

        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(request.getOtp())) {
            user.setOtpAttemptCount(user.getOtpAttemptCount() + 1);
            userRepository.save(user);
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (user.getOtpExpiryTime() != null && LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setEmailVerified(true);
        user.setOtpExpiryTime(null);
        user.setVerificationOtp(null);
        user.setOtpAttemptCount(0);

        userRepository.save(user);
    }

    public void resendVerification(ResendVerificationRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.isEmailVerified()) {
            throw new AppException(ErrorCode.USER_ALREADY_VERIFIED);
        }

        String otpCode = generateAndSaveOTP(user);

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(user.getEmail())
                .subject("Verify your email - Bookify")
                .body("Hello " + user.getUsername() + "!\nYour new verification code is: " + otpCode)
                .build();

        kafkaTemplate.send("notification-delivery", notificationEvent);
    }

    public String generateAndSaveOTP(User user) {
        if (user.getLastOtpSentTime() != null
                && LocalDateTime.now().isBefore(user.getLastOtpSentTime().plusMinutes(1))) {
            throw new AppException(ErrorCode.OTP_SEND_COOLDOWN);
        }

        String otp = String.format("%04d", rd.nextInt(10000));
        user.setVerificationOtp(otp);

        user.setOtpExpiryTime(LocalDateTime.now().plusMinutes(5));
        user.setLastOtpSentTime(LocalDateTime.now());
        user.setOtpAttemptCount(0);

        userRepository.save(user);

        return otp;
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (!userOptional.isPresent()) {
            log.warn("Attempt to reset password for non-existent email: {}", request.getEmail());
            return;
        }
        User user = userOptional.get();
        String otpCode = generateAndSaveOTP(user);

        NotificationEvent event = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(user.getEmail())
                .subject("Reset your password - Bookify")
                .body("Hello " + user.getUsername() + "!\nYour password reset code is: " + otpCode)
                .build();

        kafkaTemplate.send("notification-delivery", event);
    }

    @Transactional // quản lý transaction(nhóm các thao tác trên db thành 1 đơn vị thống nhất), nếu
    // method này gọi
    // method khác -> Spring quản lý transaction, nếu method khác có @Transactional
    // -> join vào
    // transaction của method này
    // yêu cầu chạy method trong 1 transaction với Spring quản lý, khi hết method ->
    // Spring auto commit -> unlock
    // @Clock()(nếu có lỗi -> rollback)
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findAndLockByEmail(request.getEmail()).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        });

        if (user.getOtpAttemptCount() >= 5) {
            throw new AppException(ErrorCode.TOO_MANY_OTP_ATTEMPTS);
        }

        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(request.getOtp())) {
            user.setOtpAttemptCount(user.getOtpAttemptCount() + 1);
            userRepository.save(user);
            throw new AppException(ErrorCode.INVALID_OTP);
        }

        if (user.getOtpExpiryTime() != null && LocalDateTime.now().isAfter(user.getOtpExpiryTime())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setPasswordChangeAt(LocalDateTime.now());

        user.setVerificationOtp(null);
        user.setOtpExpiryTime(null);
        user.setOtpAttemptCount(0);
        userRepository.save(user);
    }

    public AuthenticationResponse changePassword(ChangePasswordRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findById(userId).orElseThrow(() -> {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        });

        String oldPassword = request.getCurrentPassword();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new AppException(ErrorCode.WRONG_CURRENT_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setPasswordChangeAt(LocalDateTime.now());
        Date expiry =
                new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli());

        userRepository.save(user);

        var token = generateAccessToken(user);
        return AuthenticationResponse.builder()
                .accessToken(token)
                .expiryTime(expiry)
                .build();
    }

    public AuthenticationResponse authenticateWithGoogle(GoogleLoginRequest request) {
        // Verify token with GG
        GoogleIdToken idToken = verifyGoogleToken(request.getIdToken());

        // lấy claims bên trong gg id token đã xác thực ( chứa sub, email, name ,
        // email_verfied, exp, ...)
        GoogleIdToken.Payload payload = idToken.getPayload();

        // Get info from payload
        String googleId = payload.getSubject();
        String email = payload.getEmail();
        String name = (String) payload.get("name");

        // Find user by Google ID -> if not -> find by email
        User user = userRepository
                .findByGoogleId(googleId)
                .orElseGet(
                        () -> // nếu tìm bằng ggid -> k thấy -> trả Optional.empty(), thấy -> User
                        userRepository.findByEmail(email).orElse(null));

        // Link acc if user registered by Local (but not linked to GG)
        if (user != null) {
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleId);
                userRepository.save(user);
            }
        } else {
            // If user not ex -> new register

            HashSet<Role> roles = new HashSet<>();
            roleRepository.findById(com.dl1803.identity.enums.Role.USER.name()).ifPresent(roles::add);

            user = User.builder()
                    .email(email)
                    .username(email)
                    .googleId(googleId)
                    .emailVerified(payload.getEmailVerified())
                    .authProvider(AuthProvider.GOOGLE)
                    .password(passwordEncoder.encode(UUID.randomUUID().toString())) // login GG -> set passw ngẫy nhiên
                    .roles(roles)
                    .build();

            user = userRepository.save(user);

            // Create Profile bên ProfileService
            ProfileCreationRequest profileRequest = ProfileCreationRequest.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(name) // Tạm gán full name vào firstName
                    .build();
            try {
                profileClient.createProfile(profileRequest);
            } catch (Exception e) {
                log.error("Failed to create profile for user {}", user.getId(), e);
                // Bạn có thể quyết định ném lỗi hay bỏ qua
            }
        }

        // Generate token hệ thống
        var accessToken = generateAccessToken(user);
        var refreshToken = generateRefreshToken(user);
        Date expiry = new Date(java.time.Instant.now()
                .plus(VALID_DURATION, java.time.temporal.ChronoUnit.SECONDS)
                .toEpochMilli());

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiryTime(expiry)
                .build();
    }

    // nhận idtoken từ FE gửi lên -> xác thực tính hợp lệ từ google
    private GoogleIdToken verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder( // obj kiểm tra token.
                            new NetHttpTransport(), // obj cung cấp http transport layer -> auto fetch public key trên
                            // internet
                            GsonFactory.getDefaultInstance() // trình giải mã json -> lấy payload token
                            )
                    .setAudience(Collections.singletonList(
                            googleClientId)) // kiểm tra token có đúng do GG cấp cho ứng dụng hện tại kh
                    .build();

            // GoogleIdToken : gg id token sau khi đã xác thực
            // verify() thực hiện kiểm tra signature, expiration time, audience, issuer, ...
            // -> trả về GoogleIdToken nếu hợp lệ, null nếu không hợp lệ
            GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);

            if (idToken == null) {
                throw new AppException(ErrorCode.INVALID_GOOGLE_TOKEN);
            }
            return idToken;
        } catch (IOException | GeneralSecurityException e) {
            log.error("Google verify error: ", e);
            throw new AppException(ErrorCode.GOOGLE_AUTH_FAILED);
        }
    }
}
