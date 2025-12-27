package com.example.timeflow.ui.register.step;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timeflow.R;
import com.example.timeflow.api.ApiClient;
import com.example.timeflow.api.AuthApi;
import com.example.timeflow.requestandresponse.ApiResponse;
import com.example.timeflow.requestandresponse.EmailCodeScene;
import com.example.timeflow.requestandresponse.SendEmailCodeRequest;
import com.example.timeflow.requestandresponse.VerifyCodeRequest;
import com.example.timeflow.viewmodel.RegisterViewModel;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Step1EmailFragment extends Fragment {

    private RegisterViewModel viewModel;
    private AuthApi authApi;
    private Button btnSendCode;
    private int countdown = 60;
    private boolean isCounting = false;

    // 1. 定义正则表达式常量
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    // 2. 校验方法
    private boolean isValidEmail(String email) {
        return email != null && email.matches(EMAIL_PATTERN);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_step1, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);
        authApi = ApiClient.getInstance(requireContext()).create(AuthApi.class);

        EditText etEmail = view.findViewById(R.id.etEmail);
        EditText etCode = view.findViewById(R.id.etCode);
        btnSendCode = view.findViewById(R.id.btnSendCode);
        Button btnNext = view.findViewById(R.id.btnNext);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // 填充已输入的数据
        if (!viewModel.email.isEmpty()) {
            etEmail.setText(viewModel.email);
        }
        if (!viewModel.code.isEmpty()) {
            etCode.setText(viewModel.code);
        }

        // 返回按钮
        btnBack.setOnClickListener(v -> requireActivity().finish());

        btnSendCode.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                etEmail.setError("请输入邮箱");
                return;
            }
            if (!isValidEmail(email)) {
                etEmail.setError("请先输入正确的邮箱");
                return;
            }
            if (isCounting) {
                Toast.makeText(getContext(), "请等待倒计时结束", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("Register", "开始发送验证码请求，邮箱: " + email);

            SendEmailCodeRequest request = new SendEmailCodeRequest(email, EmailCodeScene.REGISTER);
            authApi.sendEmailCode(request).enqueue(new Callback<ApiResponse<String>>() {
                @Override
                public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                    Log.d("Register", "收到响应 - HTTP状态码: " + response.code() + ", 成功: " + response.isSuccessful());

                    if (response.isSuccessful()) {
                        if (response.body() != null) {
                            Log.d("Register", "响应体 - code: " + response.body().getCode() + ", message: " + response.body().getMessage());

                            if (response.body().getCode() == 0) {
                                Log.d("Register", "验证码发送成功，开始倒计时");
                                Toast.makeText(getContext(), "验证码已发送到邮箱", Toast.LENGTH_SHORT).show();
                                startCountdown();
                            } else {
                                String errorMsg = response.body().getMessage();
                                Log.d("Register", "业务逻辑失败: " + errorMsg);
                                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e("Register", "响应体为null");
                            Toast.makeText(getContext(), "发送失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("Register", "HTTP请求失败，状态码: " + response.code());
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "无错误信息";
                            Log.e("Register", "错误响应体: " + errorBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        String errorMsg = response.body() != null ? response.body().getMessage() : "发送失败";
                        Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                    Log.e("Register", "网络请求失败: " + t.getMessage(), t);
                    Toast.makeText(getContext(), "网络错误，请检查网络连接", Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnNext.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String code = etCode.getText().toString().trim();

            if (email.isEmpty()) {
                etEmail.setError("请输入邮箱");
                return;
            }
            if(!isValidEmail(email)){
                etEmail.setError("请先输入正确的邮箱");
                return;
            }
            if (code.isEmpty() || code.length() <= 5) {
                etCode.setError("请输入验证码");
                return;
            }
            // 保存数据到ViewModel
            viewModel.email = email;
            viewModel.code = code;

            // 验证验证码
            verifyCodeAndProceed(email, code);
        });

        return view;
    }

    private void verifyCodeAndProceed(String email, String code) {
        VerifyCodeRequest request = new VerifyCodeRequest(email, EmailCodeScene.REGISTER, code);

        authApi.verifyCode(request).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        if (response.body().getCode() == 0) {
                            // 验证码验证成功
                            goToStep2();
                        } else {
                            String errorMsg = response.body().getMessage() != null ?
                                    response.body().getMessage() : "验证码错误或已过期";
                            Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "服务器响应异常", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // HTTP状态码非200系列的情况
                    String errorMsg;
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            errorMsg = "验证失败: " + response.code() + " - " + errorBody;
                        } else {
                            errorMsg = "验证失败，状态码: " + response.code();
                        }
                    } catch (IOException e) {
                        errorMsg = "验证失败，状态码: " + response.code();
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCountdown() {
        isCounting = true;
        btnSendCode.setEnabled(false);

        new android.os.Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (countdown > 0) {
                    btnSendCode.setText(countdown + "秒后重发");
                    countdown--;
                    new android.os.Handler().postDelayed(this, 1000);
                } else {
                    btnSendCode.setText("获取验证码");
                    btnSendCode.setEnabled(true);
                    countdown = 60;
                    isCounting = false;
                }
            }
        }, 1000);
    }

    private void goToStep2() {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.slide_out_left, R.anim.slide_out_left)
                .replace(R.id.registerContainer, new Step2AccountFragment())
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isCounting = false;
        countdown = 60;
    }
}