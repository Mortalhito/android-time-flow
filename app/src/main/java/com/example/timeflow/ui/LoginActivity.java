package com.example.timeflow.ui;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timeflow.R;
import com.example.timeflow.api.ApiClient;
import com.example.timeflow.api.AuthApi;
import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.JwtResponse;
import com.example.timeflow.room.entity.LoginRequest;
import com.example.timeflow.room.entity.User;

import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private AuthApi authApi;
    private UserDao userDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initData();
        setupActions();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
    }

    private void initData() {
        authApi = ApiClient.getInstance(this).create(AuthApi.class);
        userDao = AppDatabase.getInstance(this).userDao();
    }

    private void setupActions() {

        findViewById(R.id.btnLogin).setOnClickListener(v -> login());

        findViewById(R.id.btnRegister).setOnClickListener(v ->
                Toast.makeText(this, "注册功能待实现", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.btnForgot).setOnClickListener(v ->
                Toast.makeText(this, "找回密码功能待实现", Toast.LENGTH_SHORT).show()
        );
    }

    private void login() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(username, password);

        authApi.login(request).enqueue(new Callback<JwtResponse>() {
            @Override
            public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {

                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(LoginActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                    return;
                }

                String token = response.body().getToken();

                saveUserToRoom(token, username);
            }

            @Override
            public void onFailure(Call<JwtResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserToRoom(String token, String username) {

        Executors.newSingleThreadExecutor().execute(() -> {

            User user = new User();
            user.username = username;
            user.email = ""; // 后续可从 /me 接口补全
            user.token = token;

            userDao.clear();
            userDao.saveUser(user);

            runOnUiThread(() -> {
                Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
                finish(); // 返回 ProfileFragment
            });
        });
    }
}
