package com.example.timeflow.ui.register.step;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.timeflow.R;
import com.example.timeflow.api.ApiClient;
import com.example.timeflow.api.AuthApi;
import com.example.timeflow.datastore.TokenManager;
import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.requestandresponse.JwtResponse;
import com.example.timeflow.room.entity.User;
import com.example.timeflow.requestandresponse.UserRegisterRequest;
import com.example.timeflow.viewmodel.RegisterViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Step3ProfileFragment extends Fragment {

    private static final int PICK_IMAGE = 1001;

    private RegisterViewModel viewModel;
    private AuthApi authApi;
    private Uri avatarUri;
    private ImageView ivAvatar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_step3, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(RegisterViewModel.class);
        authApi = ApiClient.getInstance(requireContext()).create(AuthApi.class);

        ivAvatar = view.findViewById(R.id.ivAvatar);
        EditText etNickname = view.findViewById(R.id.etNickname);
        Button btnRegister = view.findViewById(R.id.btnRegister);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        // 填充已输入的数据
        if (!viewModel.nickname.isEmpty()) {
            etNickname.setText(viewModel.nickname);
        }
        if (viewModel.avatarUri != null) {
            avatarUri = viewModel.avatarUri;
            Glide.with(this).load(avatarUri).into(ivAvatar);
        }

        // 返回按钮
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        ivAvatar.setOnClickListener(v -> pickImage());

        btnRegister.setOnClickListener(v -> {
            String nickname = etNickname.getText().toString().trim();
            if (nickname.isEmpty()) {
                etNickname.setError("请输入昵称");
                return;
            }

            viewModel.nickname = nickname;
            viewModel.avatarUri = avatarUri;

            submitRegister();
        });

        return view;
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    private void submitRegister() {
        // 创建 UserRegisterRequest 对象
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername(viewModel.username);
        registerRequest.setPassword(viewModel.password);
        registerRequest.setEmail(viewModel.email);
        registerRequest.setNickname(viewModel.nickname);

        // 处理头像 - 这里需要根据后端要求处理
        // 方案1: 如果后端支持文件上传，需要先上传头像获取URL
        // 方案2: 如果后端支持base64，将头像转换为base64字符串
        // 方案3: 如果头像可选，可以先传空或默认值
        if (avatarUri != null) {
            // 这里需要实现头像处理逻辑
            String avatarBase64 = convertImageToBase64(avatarUri);
            registerRequest.setAvatar(avatarBase64);
        } else {
            registerRequest.setAvatar(""); // 或者传 null，根据后端要求
        }

        // 调用注册接口
        authApi.register(registerRequest).enqueue(registerCallback);
    }

    private String convertImageToBase64(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[inputStream.available()];
            inputStream.read(bytes);
            inputStream.close();

            String base64 = Base64.encodeToString(bytes, Base64.DEFAULT);
            return "data:image/jpeg;base64," + base64; // 根据实际图片格式调整

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Callback<JwtResponse> registerCallback = new Callback<JwtResponse>() {
        @Override
        public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
            if (response.isSuccessful() && response.body() != null) {
                // 注册成功，保存token和用户信息
                String token = response.body().getToken();
                saveUserAndToken(token);
                Toast.makeText(getContext(), "注册成功", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            } else {
                Toast.makeText(getContext(), "注册失败", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFailure(Call<JwtResponse> call, Throwable t) {
            Toast.makeText(getContext(), "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void saveUserAndToken(String token) {
        new Thread(() -> {
            // 保存token到SharedPreferences
            TokenManager tokenManager = new TokenManager(requireContext());
            tokenManager.saveToken(token);

            // 保存用户信息到数据库
            UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
            User user = new User();
            user.setUsername(viewModel.username);
            user.setEmail(viewModel.email);
            user.setNickname(viewModel.nickname);
            user.setToken(token);

            userDao.clear();
            userDao.saveUser(user);
        }).start();
    }

    private File uriToFile(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            File file = new File(requireContext().getCacheDir(), "avatar_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.close();
            inputStream.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            avatarUri = data.getData();
            Glide.with(this).load(avatarUri).into(ivAvatar);
        }
    }
}