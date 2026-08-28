package com.example.bookify.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VerifyEmailRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("otp")
    private String otp;

    public VerifyEmailRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }


}
