package com.example.timeflow.ui;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.fragment.app.Fragment;

import com.example.timeflow.R;
import com.example.timeflow.service.FocusDeviceAdminReceiver;
import com.example.timeflow.service.FocusModeService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class FocusFragment extends Fragment {

    private NumberPicker hourPicker, minutePicker;
    private Button btnStartFocus;
    private DevicePolicyManager devicePolicyManager;
    private ComponentName adminComponent;

    public FocusFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_focus, container, false);

        initViews(view);
        setupNumberPickers();
        setClickListeners();
        setupDeviceAdmin();

        return view;
    }

    private void initViews(View view) {
        hourPicker = view.findViewById(R.id.hourPicker);
        minutePicker = view.findViewById(R.id.minutePicker);
        btnStartFocus = view.findViewById(R.id.btnStartFocus);
    }

    private void setupNumberPickers() {
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(12);
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

    private void startFocusMode() {
        int hours = hourPicker.getValue();
        int minutes = minutePicker.getValue();
        int totalMinutes = hours * 60 + minutes;

        if (totalMinutes > 0) {
            if (devicePolicyManager.isAdminActive(adminComponent)) {
                // 启动专注模式服务
                Intent serviceIntent = new Intent(getActivity(), FocusModeService.class);
                serviceIntent.putExtra("duration_minutes", totalMinutes);
                getActivity().startService(serviceIntent);

                // 锁定设备
                devicePolicyManager.lockNow();
            } else {
                showAdminPermissionDialog();
            }
        }
    }

    private void showAdminPermissionDialog() {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("设备管理员权限")
                .setMessage("需要设备管理员权限来启用专注模式")
                .setPositiveButton("授权", (dialog, which) -> requestAdminPermission())
                .setNegativeButton("取消", null)
                .show();
    }

    private void requestAdminPermission() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
        startActivity(intent);
    }
}