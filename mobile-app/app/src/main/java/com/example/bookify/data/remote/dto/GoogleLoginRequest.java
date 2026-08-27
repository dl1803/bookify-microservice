package com.example.bookify.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class GoogleLoginRequest {
    @SerializedName("idToken")
    String idToken;

    public GoogleLoginRequest(String idToken) {
        this.idToken = idToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
}
