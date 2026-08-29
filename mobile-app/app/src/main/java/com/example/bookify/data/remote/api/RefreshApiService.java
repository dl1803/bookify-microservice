package com.example.bookify.data.remote.api;

import com.example.bookify.data.remote.dto.ApiResponse;
import com.example.bookify.data.remote.dto.AuthenticationResponse;
import com.example.bookify.data.remote.dto.RefreshRequest;

import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RefreshApiService {
    @POST("/api/v1/identity/auth/refresh")
    Single<ApiResponse<AuthenticationResponse>> refreshToken(@Body RefreshRequest request);
}
