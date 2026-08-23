package com.example.bookify.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.bookify.data.model.UserModel;
import com.example.bookify.data.remote.api.IdentityApiService;
import com.example.bookify.data.remote.dto.ForgotPasswordRequest;
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
    private final MutableLiveData<UserModel> currentUserLiveData = new MutableLiveData<>();

    @Inject
    public AuthRepository(IdentityApiService identityApiService, TokenManager tokenManager) {
        this.identityApiService = identityApiService;
        this.tokenManager = tokenManager;
    }

    public LiveData<UserModel> getCurrentUser() {
        return currentUserLiveData;
    }

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
                        throw new Exception(response.getMessage() != null ? response.getMessage() : "Login failed");
                    }
                    // Save token
                    tokenManager.saveToken(response.getResult().getToken());
                    
                    UserModel user = new UserModel(username, "", "", "", "", "", "");
                    currentUserLiveData.postValue(user);
                    return user;
                });
    }

    public Single<ApiResponse<UserResponse>> register(UserCreationRequest request) {
        return identityApiService.registerUser(request);
    }

    public void logout() {
        tokenManager.clearToken();
        currentUserLiveData.setValue(null);
    }

    public Single<ApiResponse<Void>> verifyEmail(String email, String otp) {
        return identityApiService.verifyEmail(new VerifyEmailRequest(email, otp));
    }

    public Single<ApiResponse<Void>> resendVerification(String email) {
        return identityApiService.resendVerification(new ResendVerificationRequest(email));
    }

    public Single<ApiResponse<Void>> forgotPassword(String email) {
        return identityApiService.forgotPassword(new ForgotPasswordRequest(email));
    }

    public Single<ApiResponse<Void>> resetPassword(String email, String otp, String newPassword) {
        return identityApiService.resetPassword(new ResetPasswordRequest(email, otp, newPassword));
    }
}
