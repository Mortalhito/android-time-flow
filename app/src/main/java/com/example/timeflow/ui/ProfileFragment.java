package com.example.timeflow.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.timeflow.R;
import com.example.timeflow.room.entity.User;
import com.example.timeflow.view.CircleImageView;
import com.example.timeflow.viewmodel.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private CircleImageView ivAvatar;
    private TextView tvUserName, tvUserEmail, tvFocusStats, tvHabitStats;
    private View layoutStats, btnEdit, btnSettings, btnLogout;

    private ProfileViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        observeUser();
        setupActions();

        return view;
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvFocusStats = view.findViewById(R.id.tvFocusStats);
        tvHabitStats = view.findViewById(R.id.tvHabitStats);

        layoutStats = view.findViewById(R.id.layoutStats);
        btnEdit = view.findViewById(R.id.btnEditProfile);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnLogout = view.findViewById(R.id.btnLogout);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
    }

    private void observeUser() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) {
                showNotLoginUI();
            } else {
                showLoginUI(user);
            }
        });
    }

    private void showNotLoginUI() {
        ivAvatar.setImageResource(R.drawable.default_avatar);
        tvUserName.setText("点击头像登录");
        tvUserEmail.setVisibility(View.GONE);

        layoutStats.setVisibility(View.GONE);
        btnEdit.setVisibility(View.GONE);
        btnSettings.setVisibility(View.GONE);
        btnLogout.setVisibility(View.GONE);

        ivAvatar.setOnClickListener(v ->
                startActivity(new Intent(getContext(), LoginActivity.class))
        );
    }

    private void showLoginUI(User user) {
        tvUserName.setText(user.getUsername());
        tvUserEmail.setText(user.getEmail());
        tvUserEmail.setVisibility(View.VISIBLE);

        layoutStats.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.VISIBLE);

        tvFocusStats.setText("总专注时间：12 小时");
        tvHabitStats.setText("习惯完成率：75%");

        // --- 核心修改：显示 Base64 头像 ---
        if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(user.getAvatar(), Base64.NO_WRAP);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                ivAvatar.setImageBitmap(decodedByte);
            } catch (Exception e) {
                ivAvatar.setImageResource(R.drawable.default_avatar);
            }
        } else {
            ivAvatar.setImageResource(R.drawable.default_avatar);
        }

        // --- 核心修改：登录后点击头像不再跳转 LoginActivity ---
        ivAvatar.setOnClickListener(v -> {
            // 这里可以写“点击查看大图”或者“更换头像”的逻辑
            Toast.makeText(getContext(), "您已登录", Toast.LENGTH_SHORT).show();
        });

        // 显示其他 UI 元素
        layoutStats.setVisibility(View.VISIBLE);
        btnEdit.setVisibility(View.VISIBLE);
        btnSettings.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.VISIBLE);
    }

    private void setupActions() {
        btnLogout.setOnClickListener(v ->
                viewModel.logout(() ->
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(getContext(), "已退出登录", Toast.LENGTH_SHORT).show()
                        )
                )
        );
    }
}
