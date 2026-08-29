package com.example.bookify.data.remote.api;

import com.example.bookify.data.remote.dto.ApiResponse;
import com.example.bookify.data.remote.dto.AuthenticationRequest;
import com.example.bookify.data.remote.dto.AuthenticationResponse;
import com.example.bookify.data.remote.dto.ForgotPasswordRequest;
import com.example.bookify.data.remote.dto.GoogleLoginRequest;
import com.example.bookify.data.remote.dto.LogoutRequest;
import com.example.bookify.data.remote.dto.RefreshRequest;
import com.example.bookify.data.remote.dto.ResendVerificationRequest;
import com.example.bookify.data.remote.dto.ResetPasswordRequest;
import com.example.bookify.data.remote.dto.UserCreationRequest;
import com.example.bookify.data.remote.dto.UserResponse;
import com.example.bookify.data.remote.dto.VerifyEmailRequest;
import com.example.bookify.data.remote.dto.VerifyResetOtpResponse;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IdentityApiService {
    @POST("/api/v1/identity/users/registration")
    Single<ApiResponse<UserResponse>> registerUser(@Body UserCreationRequest request);

    @POST("/api/v1/identity/auth/token")
    Single<ApiResponse<AuthenticationResponse>> login(@Body AuthenticationRequest request);

    @POST("/api/v1/identity/auth/verify-email")
    Single<ApiResponse<Void>> verifyEmail(@Body VerifyEmailRequest request);

    @POST("/api/v1/identity/auth/resend-verification")
    Single<ApiResponse<Void>> resendVerification(@Body ResendVerificationRequest request);

    @POST("/api/v1/identity/auth/forgot-password")
    Single<ApiResponse<Void>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("/api/v1/identity/auth/reset-password")
    Single<ApiResponse<Void>> resetPassword(@Body ResetPasswordRequest request);

    @POST("/api/v1/identity/auth/verify-reset-otp")
    Single<ApiResponse<VerifyResetOtpResponse>> verifyResetOtp(@Body VerifyEmailRequest request);

    @POST("/api/v1/identity/auth/google")
    Single<ApiResponse<AuthenticationResponse>> loginWithGoogle(@Body GoogleLoginRequest request);

    @POST("/api/v1/identity/auth/logout")
    Single<ApiResponse<Void>> logout(@Body LogoutRequest request);
}
