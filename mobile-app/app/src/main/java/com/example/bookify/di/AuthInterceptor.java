package com.example.bookify.di;

import androidx.annotation.NonNull;

import com.example.bookify.utils.TokenManager;

import java.io.IOException;

import javax.inject.Inject;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;

    @Inject
    public AuthInterceptor(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    // sẽ được gọi mỗi khi có request đi qua interceptor
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request(); // lấy request hện tại
        Request.Builder builder = original.newBuilder(); // tạo builder để tạo request mới dựa trên rq cũ

        String token = tokenManager.getToken();
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token); // thêm header vào rq mới
        }

        return chain.proceed(builder.build()); // build rq và đưa tiếp tục đi qua chain để gửi rq tới server
        // và nhận kết quả trả về Response để đưa repo xử lí
    }
}
