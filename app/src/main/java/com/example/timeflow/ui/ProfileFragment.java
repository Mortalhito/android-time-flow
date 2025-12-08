package com.example.timeflow.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.timeflow.R;
import com.example.timeflow.utils.SharedPreferencesManager;
import com.example.timeflow.view.CircleImageView;

public class ProfileFragment extends Fragment {

    private CircleImageView ivAvatar;
    private TextView tvUserName, tvUserEmail, tvFocusStats, tvHabitStats;
    private SharedPreferencesManager prefsManager;
    private View rootView; // 添加成员变量保存view

    public ProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_profile, container, false); // 保存到成员变量

        initViews(rootView);
        loadUserData();
        setClickListeners();

        return rootView;
    }

    private void initViews(View view) {
        ivAvatar = view.findViewById(R.id.ivAvatar);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvFocusStats = view.findViewById(R.id.tvFocusStats);
        tvHabitStats = view.findViewById(R.id.tvHabitStats);
        prefsManager = new SharedPreferencesManager(getContext());
    }

    private void loadUserData() {
        // 从SharedPreferences加载用户数据
        tvUserName.setText(prefsManager.getUserName());
        tvUserEmail.setText(prefsManager.getUserEmail());

        // 计算统计数据
        long totalFocusMinutes = prefsManager.getTotalFocusTime();
        long totalFocusHours = totalFocusMinutes / 60;
        tvFocusStats.setText("总专注时间: " + totalFocusHours + "小时");

        // 计算习惯完成率
        float habitCompletionRate = calculateHabitCompletionRate();
        tvHabitStats.setText("习惯完成率: " + String.format("%.0f", habitCompletionRate) + "%");
    }

    private float calculateHabitCompletionRate() {
        // 这里计算所有习惯的平均完成率
        // 简化实现，实际应该从数据库计算
        return 75.0f; // 示例值
    }

    private void setClickListeners() {
        // 头像点击事件
        ivAvatar.setOnClickListener(v -> changeAvatar());

        // 编辑个人信息按钮 - 使用rootView
        rootView.findViewById(R.id.btnEditProfile).setOnClickListener(v -> editProfile());

        // 设置按钮 - 使用rootView
        rootView.findViewById(R.id.btnSettings).setOnClickListener(v -> openSettings());

        // 退出登录按钮 - 使用rootView
        rootView.findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void changeAvatar() {
        // 更换头像的逻辑
        android.widget.Toast.makeText(getContext(), "更换头像功能开发中", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void editProfile() {
        // 编辑个人信息的逻辑
        android.widget.Toast.makeText(getContext(), "编辑个人信息功能开发中", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void openSettings() {
        // 打开设置的逻辑
        android.widget.Toast.makeText(getContext(), "设置功能开发中", android.widget.Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        // 退出登录的逻辑
        new android.app.AlertDialog.Builder(getContext())
                .setTitle("确认退出")
                .setMessage("您确定要退出登录吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 执行退出登录操作
                    android.widget.Toast.makeText(getContext(), "已退出登录", android.widget.Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }
}