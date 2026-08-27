// WHAT: Trạm gác 401 chuẩn Clean Architecture
package com.example.bookify.di;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bookify.data.remote.api.RefreshApiService;
import com.example.bookify.data.remote.dto.ApiResponse;
import com.example.bookify.data.remote.dto.AuthenticationResponse;
import com.example.bookify.data.remote.dto.RefreshRequest;
import com.example.bookify.utils.TokenManager;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class TokenRefreshAuthenticator implements Authenticator {

    private final TokenManager tokenManager;
    private final RefreshApiService refreshApiService;

    @Inject
    public TokenRefreshAuthenticator(TokenManager tokenManager, RefreshApiService refreshApiService) {
        this.tokenManager = tokenManager;
        this.refreshApiService = refreshApiService;
    }

    // hàm sẽ được thực thi khi hàm OkHttp gọi authentication thất bại 401
    // response para method: là response của rq ban đầu (OkHttp Response)
    // chứa rq cũ, method http, authorization bearer.
    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        String refreshToken = tokenManager.getRefreshToken();

        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            tokenManager.notifySessionExpired(); // buộc login lại do hết session
            return null; // Trường hợp không có refresh token -> hủy retry
        }

        try {
            RefreshRequest refreshRequest = new RefreshRequest(refreshToken);

            // Response của gọi API refresh token
            ApiResponse<AuthenticationResponse> apiResponse = refreshApiService
                    .refreshToken(refreshRequest)
                    .blockingGet(); // Sử dụng blockingGet() để đợi kết quả trả về mới chạy tiếp
            // do Authenticator.authenticate() là API synchronous của OkHttp -> cần trả kq
            // ngay trong hàm
            // mà hiện tại đang dùng RxJava để thực hiện các API bất đồng bộ
            // từ Single<ApiResponse> -> blockingGet() -> ApiResponse<>

            if (apiResponse != null && apiResponse.getCode() == 1000 && apiResponse.getResult() != null) {
                String newAccessToken = apiResponse.getResult().getAccessToken();
                String newRefreshToken = apiResponse.getResult().getRefreshToken();

                // lưu token
                tokenManager.saveTokens(newAccessToken, newRefreshToken);

                // tạo request mới dựa trên request cũ với token mới bằng Builder -> retry
                return response.request().newBuilder()
                        .header("Authorization", "Bearer " + newAccessToken)
                        .build(); // trả rq mới cho OkHttp để retry
            } else {
                tokenManager.notifySessionExpired();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            tokenManager.notifySessionExpired();
            return null;
        }
    }
}
