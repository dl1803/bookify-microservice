package com.example.bookify.utils;

import com.example.bookify.data.remote.dto.ApiResponse;
import com.google.gson.Gson;

import retrofit2.HttpException;

public class ErrorUtils {

    private static final Gson gson = new Gson();

    /**
     * Parses a Throwable (especially Retrofit's HttpException) to extract the actual 
     * JSON error message returned from the Spring Boot Backend.
     */
    public static String parseError(Throwable throwable, String defaultMessage) {
        if (throwable instanceof HttpException) {
            HttpException httpException = (HttpException) throwable;
            if (httpException.response() != null && httpException.response().errorBody() != null) {
                try {
                    String errorBody = httpException.response().errorBody().string();
                    ApiResponse<?> apiResponse = gson.fromJson(errorBody, ApiResponse.class);
                    if (apiResponse != null && apiResponse.getMessage() != null) {
                        return apiResponse.getMessage();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        // Default fallback if parsing fails or it's not an HttpException (e.g. network timeout)
        return throwable.getMessage() != null ? throwable.getMessage() : defaultMessage;
    }
}
