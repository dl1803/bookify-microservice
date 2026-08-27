package com.dl1803.identity.controller;

import java.text.ParseException;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dl1803.identity.dto.request.*;
import com.dl1803.identity.dto.response.ApiResponse;
import com.dl1803.identity.dto.response.AuthenticationResponse;
import com.dl1803.identity.dto.response.IntrospectResponse;
import com.dl1803.identity.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import lombok.*;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder().result(result).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authenticationService.verifyEmail(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Email verified successfully")
                .build();
    }

    @PostMapping("/resend-verification")
    public ApiResponse<Void> resendVerification(@RequestBody ResendVerificationRequest request) {
        authenticationService.resendVerification(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Verification email sent")
                .build();
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        authenticationService.forgotPassword(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("If the email exists in our system, an OTP has been sent.")
                .build();
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Password has been reset")
                .build();
    }

    @PostMapping("/change-password")
    public ApiResponse<AuthenticationResponse> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        AuthenticationResponse result = authenticationService.changePassword(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .message("Password changed successfully")
                .result(result)
                .build();
    }

    @PostMapping("/google")
    public ApiResponse<AuthenticationResponse> authenticateWithGoogle(@RequestBody @Valid GoogleLoginRequest request) {
        var result = authenticationService.authenticateWithGoogle(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .code(1000)
                .message("Google login successful")
                .result(result)
                .build();
    }
}
