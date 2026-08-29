package com.example.bookify.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class VerifyResetOtpResponse {
    @SerializedName("resetToken")
    private String resetToken;

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }
}
