package com.example.timeflow.ui.register.step;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.example.timeflow.MainActivity;
import com.example.timeflow.R;
import com.example.timeflow.api.ApiClient;
import com.example.timeflow.api.AuthApi;
import com.example.timeflow.requestandresponse.JwtResponse;
import com.example.timeflow.requestandresponse.UserRegisterRequest;
import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.datastore.TokenManager;
import com.example.timeflow.room.entity.User;
import com.example.timeflow.viewmodel.RegisterViewModel;

import java.io.ByteArrayOutputStream;
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
        String nickname = viewModel.nickname.trim();
        if (nickname.isEmpty()) {
            Toast.makeText(getContext(), "请输入昵称", Toast.LENGTH_SHORT).show();
            return;
        }
        viewModel.nickname = nickname;

        // --- 核心修改部分：压缩图片 ---
        String avatarBase64 = "";
        if (avatarUri != null) {
            avatarBase64 = compressAndEncodeImage(avatarUri);
        }

        // 构建请求对象
        UserRegisterRequest registerRequest = new UserRegisterRequest();
        registerRequest.setUsername(viewModel.username);
        registerRequest.setPassword(viewModel.password);
        registerRequest.setEmail(viewModel.email);
        registerRequest.setNickname(viewModel.nickname);
        registerRequest.setAvatar(avatarBase64); // 这里是压缩后的极短字符串

        // 调用注册接口
        authApi.register(registerRequest).enqueue(registerCallback);
    }

    private String compressAndEncodeImage(Uri uri) {
        try {
            // 1. 获取原始图片的输入流
            InputStream is = requireContext().getContentResolver().openInputStream(uri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(is);

            // 2. 缩小尺寸：头像不需要太清晰，200x200 像素足够了
            // 这步能减少 90% 以上的字符长度
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, 200, 200, true);

            // 3. 质量压缩
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // 50 表示 50% 的压缩质量

            byte[] bytes = baos.toByteArray();

            // 4. 清理内存
            if (is != null) is.close();
            originalBitmap.recycle();
            scaledBitmap.recycle();

            // 5. 转回 Base64
            return Base64.encodeToString(bytes, Base64.NO_WRAP);

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String encodeImage(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            // 这里建议先压缩，否则 Base64 太长可能存入失败（哪怕数据库改了 LONGTEXT，内存开销也大）
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 120, 120, true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            byte[] bytes = baos.toByteArray();
            return Base64.encodeToString(bytes, Base64.NO_WRAP);
        } catch (Exception e) {
            return null;
        }
    }

    private Callback<JwtResponse> registerCallback = new Callback<JwtResponse>() {
        @Override
        public void onResponse(Call<JwtResponse> call, Response<JwtResponse> response) {
            // 建议修改后的逻辑
            if (response.isSuccessful() && response.body() != null) {
                String token = response.body().getToken();
                String base64Avatar = (avatarUri != null) ? encodeImage(avatarUri) : null;
                // 1. 先把 Uri 转成 Base64（在主线程做或提前准备好）

                new Thread(() -> {
                    UserDao userDao = AppDatabase.getInstance(requireContext()).userDao();
                    User user = new User();
                    user.setUsername(viewModel.username);
                    user.setEmail(viewModel.email);
                    user.setNickname(viewModel.nickname);
                    user.setToken(token);

                    // !!! 之前您这里漏掉了这一行，所以断点是 null !!!
                    user.setAvatar(base64Avatar);

                    userDao.clear();
                    userDao.saveUser(user);

                    // 保存 Token 到 DataStore/SharedPreferences
                    new TokenManager(requireContext()).saveToken(token);

                    // 跳转
                    requireActivity().runOnUiThread(() -> {
                        startActivity(new Intent(getContext(), MainActivity.class));
                        requireActivity().finish();
                    });
                }).start();
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