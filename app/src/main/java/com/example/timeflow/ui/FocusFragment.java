package com.example.timeflow.ui;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.timeflow.R;
import com.example.timeflow.repository.FocusRecordRepository;
import com.example.timeflow.service.FocusDeviceAdminReceiver;

import java.util.Date;
import java.util.Locale;

public class FocusFragment extends Fragment {

    private NumberPicker hourPicker, minutePicker;
    private Button btnStartFocus;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;
    private TextView tvTodayTotalTime; // 新增
    private FocusRecordRepository repository; // 新增
    public FocusFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus, container, false);

        initViews(view);
        setupNumberPickers();
        setClickListeners();
        setupDeviceAdmin();

        // 初始化 Repository
        repository = new FocusRecordRepository(getActivity().getApplication());

        return view;
    }

    private void initViews(View view) {
        hourPicker = view.findViewById(R.id.hourPicker);
        minutePicker = view.findViewById(R.id.minutePicker);
        btnStartFocus = view.findViewById(R.id.btnStartFocus);
        tvTodayTotalTime = view.findViewById(R.id.tvTodayTotalTime); //
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次回到主界面时刷新今日时间
        updateTodayTime();
    }

    private void updateTodayTime() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 开启子线程查询数据库
        new Thread(() -> {
            long totalMinutes = repository.getTodayTotalMinutes(today);

            // 回到主线程更新 UI
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    tvTodayTotalTime.setText("今日总专注时间：" + totalMinutes + " 分钟");
                });
            }
        }).start();
    }



    private void setupNumberPickers() {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(2);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
    }

    private void setupDeviceAdmin() {
        devicePolicyManager = (DevicePolicyManager) getActivity().getSystemService(Context.DEVICE_POLICY_SERVICE);
        adminComponent = new ComponentName(getActivity(), FocusDeviceAdminReceiver.class);
    }

    private void setClickListeners() {
        btnStartFocus.setOnClickListener(v -> startFocusMode());
    }

    // 在 FocusFragment.java 中添加/更新以下方法
    private void startFocusMode() {
        int hours = hourPicker.getValue();
        int minutes = minutePicker.getValue();
        int totalMinutes = hours * 60 + minutes;

        if (totalMinutes > 0) {
            Intent intent = new Intent(getActivity(), FocusLockActivity.class);
            intent.putExtra("minutes", totalMinutes);
            startActivity(intent);
        }
    }



}