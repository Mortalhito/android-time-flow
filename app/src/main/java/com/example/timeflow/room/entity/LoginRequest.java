package com.example.timeflow.room.entity;

public class LoginRequest {
    // Getters and Setters
    private String username;
    private String password;

    // 默认构造函数
    public LoginRequest() {}

    // 全参构造函数
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}