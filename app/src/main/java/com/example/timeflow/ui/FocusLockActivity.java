package com.example.timeflow.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timeflow.R;
import com.example.timeflow.repository.FocusRecordRepository;
import com.example.timeflow.view.FocusProgressView;

public class FocusLockActivity extends AppCompatActivity {
    private FocusProgressView progressView;
    private TextView tvCountdown;
    private CountDownTimer timer;
    private long totalTime;
    private boolean isDialing = false;
    private FocusRecordRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. 允许在锁屏上显示，并保持常亮
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setContentView(R.layout.activity_focus_lock);

        progressView = findViewById(R.id.focusProgressView);
        tvCountdown = findViewById(R.id.tvCountdown);

        int minutes = getIntent().getIntExtra("minutes", 0);
        totalTime = minutes * 60 * 1000L;

        startTimer(totalTime);

        // 紧急通话逻辑
        findViewById(R.id.btnEmergency).setOnClickListener(v -> {
            isDialing = true; // 关键：标记当前进入拨号状态
            try {
                stopLockTask(); // 解锁以允许跳转
            } catch (Exception e) {}

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }, 200);
        });

        // 初始进入时开启锁定
        new Handler(Looper.getMainLooper()).postDelayed(this::startLockTask, 500);
        repository = new FocusRecordRepository(getApplication());
    }

    private void startTimer(long millis) {
        timer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long left) {
                int h = (int) (left / 3600000);
                int m = (int) (left % 3600000 / 60000);
                int s = (int) (left % 60000 / 1000);
                tvCountdown.setText(String.format("%02d:%02d:%02d", h, m, s));
                progressView.setProgress((left / (float) totalTime) * 100f);
            }

            @Override
            public void onFinish() {
                int duration = getIntent().getIntExtra("minutes", 0);
                repository.addFocusRecord(duration);

                timer = null;
                isDialing = false;
                try { stopLockTask(); } catch (Exception e) {}
                finish();
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 只有计时器没结束，且【不是】在打紧急电话时，才拉回
        if (timer != null && !isDialing) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Intent intent = new Intent(this, FocusLockActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                        Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }, 500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 关键：从拨号盘返回，或者试图逃逸返回时，立即重新锁定
        isDialing = false;
        if (timer != null) {
            new Handler(Looper.getMainLooper()).postDelayed(this::startLockTask, 500);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 【核心修复】解决“拨号盘时滑动底条逃脱”
        // 如果计时未结束，且 Activity 彻底进入后台（不管是通过底条还是返回桌面）
        if (timer != null) {
            // 强行把自己拉回前台
            bringToFront();
        }
    }

    private void bringToFront() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(this, FocusLockActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        }, 300);
    }

    // 彻底禁用返回键
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        // 不调用 super，使物理/手势返回无效
    }
}