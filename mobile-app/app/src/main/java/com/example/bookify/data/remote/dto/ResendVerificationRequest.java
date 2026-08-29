package com.example.bookify.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ResendVerificationRequest {
    @SerializedName("email")
    private String email;

    public ResendVerificationRequest(String email) {
        this.email = email;
    }

}
