package com.example.timeflow.api;

import com.example.timeflow.requestandresponse.ApiResponse;
import com.example.timeflow.requestandresponse.JwtResponse;
import com.example.timeflow.requestandresponse.LoginRequest;
import com.example.timeflow.requestandresponse.SendEmailCodeRequest;
import com.example.timeflow.requestandresponse.UserRegisterRequest;
import com.example.timeflow.requestandresponse.VerifyCodeRequest;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("/api/auth/register")
    @Headers("Content-Type: application/json")
    Call<JwtResponse> register(@Body UserRegisterRequest userRegisterRequest);

    @POST("/api/auth/login")
    Call<JwtResponse> login(@Body LoginRequest request);

    @POST("/api/auth/logout")
    Call<ResponseBody> logout(@Header("Authorization") String token);

    @POST("/api/auth/email/send")
    Call<ApiResponse<String>> sendEmailCode(
            @Body SendEmailCodeRequest request
    );

    @POST("/api/auth/email/verify")
    Call<ApiResponse<String>> verifyCode(
            @Body VerifyCodeRequest request
    );

}
