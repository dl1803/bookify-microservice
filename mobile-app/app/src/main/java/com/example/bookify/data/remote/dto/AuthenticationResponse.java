package com.example.bookify.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class AuthenticationResponse {
    @SerializedName("accessToken")
    private String accessToken;

    @SerializedName("refreshToken")
    private String refreshToken;

    @SerializedName("expiryTime")
    private Date expiryTime;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Date getExpiryTime() {
        return expiryTime;
    }
}
