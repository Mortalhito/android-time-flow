package com.example.timeflow.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timeflow.R;
import com.example.timeflow.api.ApiClient;
import com.example.timeflow.api.AuthApi;
import com.example.timeflow.requestandresponse.JwtResponse;
import com.example.timeflow.requestandresponse.LoginRequest;
import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.datastore.TokenManager;
import com.example.timeflow.room.entity.User;
import com.example.timeflow.ui.register.RegisterActivity;

import java.util.List;
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
        // 绑定返回按钮
        ImageButton btnBack = findViewById(R.id.btnBack);

        // 点击返回按钮，结束当前 Activity，回到 ProfileFragment
        btnBack.setOnClickListener(v -> finish());


        findViewById(R.id.btnLogin).setOnClickListener(v -> login());

        // 修改注册按钮功能
        findViewById(R.id.btnRegister).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

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
            UserDao userDao = AppDatabase.getInstance(LoginActivity.this).userDao();

            // 第一步：尝试从本地数据库查找是否已有这个用户的信息（可能从注册时留下）
            User existingUser = null;
            try {
                existingUser = userDao.getCurrentUser().getValue(); // LiveData.getValue() 在后台线程可能为 null，需小心
            } catch (Exception e) {
                // ignore
            }

            // 更稳妥的方式：直接同步查询（因为我们在子线程）
            // 你可以添加一个同步查询方法到 UserDao
            // 但为了简单，这里我们先清空再重建（推荐下面最终方案）

            // 最终推荐方案：先查本地是否有同名用户
            List<User> allUsers = AppDatabase.getInstance(LoginActivity.this)
                    .userDao()
                    .getAllUsersBlocking(); // 你需要添加这个同步方法，稍后给出

            User userToSave = new User();
            userToSave.setUsername(username);
            userToSave.setToken(token);
            userToSave.setEmail("");
            userToSave.setNickname("");
            userToSave.setAvatar(""); // 默认空

            // 如果本地已有同用户名记录，保留其头像等信息
            if (allUsers != null) {
                for (User u : allUsers) {
                    if (u.getUsername().equals(username)) {
                        userToSave.setEmail(u.getEmail() != null ? u.getEmail() : "");
                        userToSave.setNickname(u.getNickname() != null ? u.getNickname() : "");
                        userToSave.setAvatar(u.getAvatar() != null ? u.getAvatar() : "");
                        break;
                    }
                }
            }

            // 清空旧数据，保存新数据（带可能恢复的头像）
            userDao.clear();
            userDao.saveUser(userToSave);

            // 可选：也保存 token 到 SharedPreferences（你的 TokenManager）
            new TokenManager(LoginActivity.this).saveToken(token);

            runOnUiThread(() -> {
                Toast.makeText(LoginActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                finish(); // 返回 ProfileFragment
            });
        });
    }
}
