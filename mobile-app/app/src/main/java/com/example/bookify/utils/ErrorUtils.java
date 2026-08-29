package com.example.bookify.utils;

import com.example.bookify.data.remote.dto.ApiResponse;
import com.google.gson.Gson;

import java.util.Map;

import retrofit2.HttpException;

public class ErrorUtils {

    private static final Gson gson = new Gson();

    public static ApiResponse<?> parseApiResponse(Throwable throwable) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            if (httpException.response() != null && httpException.response().errorBody() != null) {
                try {
                    String errorBody = httpException.response().errorBody().string();
                    // Gson auto convert { "key": "value" } Khi Gson không được chỉ định rõ kiểu dữ liệu cụ thể của T
                    // -> Gson sẽ tự động chuyển nó thành một đối tượng Map trong Java (cụ thể là Map<String, Object>)
                    return gson.fromJson(errorBody, ApiResponse.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static String parseError(Throwable throwable) {
        return parseError(throwable, "An unexpected error occurred");
    }

    public static String parseError(Throwable throwable, String defaultMessage) {
        ApiResponse<?> apiResponse = parseApiResponse(throwable);
        if (apiResponse != null && apiResponse.getMessage() != null) {
            return apiResponse.getMessage();
        }
        // Default fallback if parsing fails or it's not an HttpException (e.g. network timeout)
        return throwable.getMessage() != null ? throwable.getMessage() : defaultMessage;
    }

    public static String extractEmailFromResult(ApiResponse<?> apiResponse) {
        //Kiểm tra xem result có phải là một đối tượng dạng { "key": "value" } (Map) hay không
        if (apiResponse != null && apiResponse.getResult() instanceof Map) {
            //Lúc chạy (Runtime): Gson đã chuyển JSON { "email": "..." } thành đối tượng Map.
            //Lúc biên dịch (Compile-time): Biến result trong class ApiResponse chỉ có kiểu là Object (chưa rõ cụ thể là gì).
            // Java không cho phép gọi hàm .get() hay .containsKey() trên một Object
            // convert sang Map để use các func có sẵn của Map
            Map<?, ?> resultMap = (Map<?, ?>) apiResponse.getResult();
            if (resultMap.containsKey("email") && resultMap.get("email") != null) {
                return resultMap.get("email").toString();
            }
        }
        return null;
    }
}
