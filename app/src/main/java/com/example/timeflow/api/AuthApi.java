package com.example.timeflow.api;

import com.example.timeflow.room.entity.JwtResponse;
import com.example.timeflow.room.entity.LoginRequest;
import com.example.timeflow.room.entity.User;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/auth/register")
    Call<User> register(@Body User user);

    @POST("/api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest request);

    @POST("/api/auth/logout")
    Call<ResponseBody> logout(@Header("Authorization") String token);
}
