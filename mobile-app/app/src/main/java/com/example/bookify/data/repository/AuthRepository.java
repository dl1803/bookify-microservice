package com.example.bookify.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookify.data.model.UserModel;
import com.example.bookify.data.remote.api.IdentityApiService;
import com.example.bookify.data.remote.dto.ForgotPasswordRequest;
import com.example.bookify.data.remote.dto.GoogleLoginRequest;
import com.example.bookify.data.remote.dto.LogoutRequest;
import com.example.bookify.data.remote.dto.ResendVerificationRequest;
import com.example.bookify.data.remote.dto.ResetPasswordRequest;
import com.example.bookify.data.remote.dto.VerifyEmailRequest;
import com.example.bookify.utils.TokenManager;
import com.example.bookify.data.remote.dto.ApiResponse;
import com.example.bookify.data.remote.dto.AuthenticationRequest;
import com.example.bookify.data.remote.dto.AuthenticationResponse;
import com.example.bookify.data.remote.dto.UserCreationRequest;
import com.example.bookify.data.remote.dto.UserResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Single;

@Singleton
public class AuthRepository {

    private final IdentityApiService identityApiService;
    private final TokenManager tokenManager;

    @Inject
    public AuthRepository(IdentityApiService identityApiService, TokenManager tokenManager) {
        this.identityApiService = identityApiService;
        this.tokenManager = tokenManager;
    }

    // .map() : bắt Response(do Retrofit biến đổi) mà Single vừa phát ra -> sau đó
    // map thành kdl muốn trả về cho ViewModel/UI.
    // khi nào bên Viewmodel gọi subcribe() thì map mới chạy
    // .subscribe():
    // ViewModel subscribe vào Single
    // -> kích hoạt việc thực thi Rx chain
    // -> map() được chạy khi Single phát ra Response
    // -> chờ/nhận kết quả cuối cùng
    // + onSuccess() nếu thành công : return
    // + onError() nếu có lỗi : throw Exception
    public Single<UserModel> login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return Single.error(new Exception("Username cannot be empty"));
        }
        if (password == null || password.length() < 4) {
            return Single.error(new Exception("Password must be at least 4 characters"));
        }

        AuthenticationRequest request = new AuthenticationRequest(username, password);
        return identityApiService.login(request)
                .map(response -> {
                    if (response.getCode() != 1000 || response.getResult() == null) {
                        throw new Exception(response.getMessage() != null ? response.getMessage() : "Invalid username or password");
                    }
                    // Save token
                    String newAccess = response.getResult().getAccessToken();
                    String newRefresh = response.getResult().getRefreshToken();
                    tokenManager.saveTokens(newAccess, newRefresh);

                    UserModel user = new UserModel(username, "", "", "", "", "", "");
                    return user;
                });
    }

    public Single<UserResponse> register(UserCreationRequest request) {
        return identityApiService.registerUser(request)
                .map(response -> {
                    if (response.getCode() != 1000 || response.getResult() == null) {
                        throw new Exception(
                                response.getMessage() != null ? response.getMessage() : "Registration failed");
                    }
                    return response.getResult();
                });
    }

    public Single<Boolean> logout() {
        String accessToken = tokenManager.getAccessToken();
        String refreshToken = tokenManager.getRefreshToken();
        
        boolean hasAccessToken = accessToken != null && !accessToken.trim().isEmpty();
        boolean hasRefreshToken = refreshToken != null && !refreshToken.trim().isEmpty();

        if (!hasAccessToken && !hasRefreshToken) {
            tokenManager.clearTokens();
            return Single.just(true);
        }

        LogoutRequest request = new LogoutRequest(accessToken, refreshToken);
        return identityApiService.logout(request)
                .map(response -> {
                    tokenManager.clearTokens();
                    if (response.getCode() != 1000) {
                        throw new Exception(response.getMessage() != null ? response.getMessage() : "Logout failed");
                    }
                    return true;
                });
    }

    public Single<Boolean> verifyEmail(String email, String otp) {
        return identityApiService.verifyEmail(new VerifyEmailRequest(email, otp))
                .map(response -> {
                    if (response.getCode() != 1000) {
                        throw new Exception(
                                response.getMessage() != null ? response.getMessage() : "Verification failed");
                    }
                    return true;
                });
    }

    public Single<Boolean> resendVerification(String email) {
        return identityApiService.resendVerification(new ResendVerificationRequest(email))
                .map(response -> {
                    if (response.getCode() != 1000) {
                        throw new Exception(
                                response.getMessage() != null ? response.getMessage() : "Resend verification failed");
                    }
                    return true;
                });
    }

    public Single<Boolean> forgotPassword(String email) {
        return identityApiService.forgotPassword(new ForgotPasswordRequest(email))
                .map(response -> {
                    if (response.getCode() != 1000) {
                        throw new Exception(
                                response.getMessage() != null ? response.getMessage() : "Forgot password failed");
                    }
                    return true;
                });
    }

    public Single<String> verifyResetOtp(String email, String otp) {
        return identityApiService.verifyResetOtp(new VerifyEmailRequest(email, otp))
                .map(response -> {
                    if (response.getCode() != 1000 || response.getResult() == null) {
                        throw new Exception(
                                response.getMessage() != null ? response.getMessage() : "OTP verification failed");
                    }
                    return response.getResult().getResetToken();
                });
    }

    public Single<Boolean> resetPassword(String resetToken, String newPassword) {
        return identityApiService.resetPassword(new ResetPasswordRequest(resetToken, newPassword))
                .map(response -> {
                    if (response.getCode() != 1000) {
                        throw new Exception(
                                response.getMessage() != null ? response.getMessage() : "Reset password failed");
                    }
                    return true;
                });
    }

    public Single<UserModel> loginWithGoogle(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            return Single.error(new Exception("Google ID token cannot be empty"));
        }

        GoogleLoginRequest request = new GoogleLoginRequest(idToken);

        return identityApiService.loginWithGoogle(request)
                .map(response -> {
                    if (response.getCode() != 1000 || response.getResult() == null) {
                        String message;
                        switch (response.getCode()) {
                            case 1039:
                            case 1040:
                                message = "Google authentication failed. Please try again.";
                                break;
                            case 1041:
                                message = "This email is already registered using another method.";
                                break;
                            default:
                                message = response.getMessage() != null ? response.getMessage()
                                        : "An unexpected error occurred.";
                                break;
                        }

                        throw new Exception(message);
                    }

                    // Lưu JWT token của hệ thống
                    String newAccess = response.getResult().getAccessToken();
                    String newRefresh = response.getResult().getRefreshToken();
                    tokenManager.saveTokens(newAccess, newRefresh);

                    UserModel user = new UserModel("Google User", "", "", "", "", "", "");
                    return user;
                });
    }
}
