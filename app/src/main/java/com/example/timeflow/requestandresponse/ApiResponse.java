package com.example.timeflow.requestandresponse;

// ApiResponse.java
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    // 构造函数、getter和setter
    public ApiResponse() {}

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // getter和setter方法
    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}