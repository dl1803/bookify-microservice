package com.example.bookify.data.remote.dto;

public class VerifyEmailRequest {
    private String email;
    private String otp;

    public VerifyEmailRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }


}
