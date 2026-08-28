package com.example.bookify.data.remote.dto;

import java.util.List;

public class ApiResponse<T> {
    private int code;
    private String error;
    private String message;
    private T result;

    public ApiResponse() {}

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getResult() { return result; }
    public void setResult(T result) { this.result = result; }
}
