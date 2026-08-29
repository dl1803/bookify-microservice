package com.example.bookify.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class ResetPasswordRequest {
    @SerializedName("resetToken")
    private String resetToken;

    @SerializedName("newPassword")
    private String newPassword;

    public ResetPasswordRequest(String resetToken, String newPassword) {
        this.resetToken = resetToken;
        this.newPassword = newPassword;
    }
}
