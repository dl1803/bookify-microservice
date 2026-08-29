package com.example.bookify.data.remote.api;

import com.example.bookify.data.remote.dto.ApiResponse;
import com.example.bookify.data.remote.dto.UpdateProfileRequest;
import com.example.bookify.data.remote.dto.UserProfileResponse;

import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Part;

public interface ProfileApiService {
    
    @GET("/api/v1/profile/users/my-profile")
    Single<ApiResponse<UserProfileResponse>> getMyProfile();

    @PUT("/api/v1/profile/users/my-profile")
    Single<ApiResponse<UserProfileResponse>> updateMyProfile(@Body UpdateProfileRequest request);

    @Multipart
    @PUT("/api/v1/profile/users/avatar")
    Single<ApiResponse<UserProfileResponse>> updateAvatar(@Part MultipartBody.Part file);
}
