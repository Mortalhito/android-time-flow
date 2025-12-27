package com.example.timeflow.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timeflow.R;
import com.example.timeflow.room.repository.FocusRecordRepository;
import com.example.timeflow.view.FocusProgressView;

import java.text.SimpleDateFormat;

public class FocusLockActivity extends AppCompatActivity {
    private FocusProgressView progressView;
    private TextView tvCountdown;
    private CountDownTimer timer;
    private long totalTime;
    private FocusRecordRepository repository;
    private ImageButton btnPauseResume;
    private long remainingMillis;
    private boolean isPaused = false;
    private ImageButton btnExit;
    private ImageView ivBackground;
    private AnimatorSet breathingAnimator; // 提升为成员变量

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus_lock);

        // 1. 初始化所有视图变量
        progressView = findViewById(R.id.focusProgressView);
        tvCountdown = findViewById(R.id.tvCountdown);
        ivBackground = findViewById(R.id.ivBackground);
        btnExit = findViewById(R.id.btnExit);
        btnPauseResume = findViewById(R.id.btnPauseResume); // 修复：之前漏了这一行

        // 2. 数据初始化
        int minutes = getIntent().getIntExtra("minutes", 0);
        totalTime = minutes * 60 * 1000L;
        remainingMillis = totalTime;
        repository = new FocusRecordRepository(getApplication());

        // 3. 设置随机背景
        int randomNum = (int) (Math.random() * 3) + 1;
        int resId = getResources().getIdentifier("tomato" + randomNum, "drawable", getPackageName());
        if (resId != 0 && ivBackground != null) {
            ivBackground.setImageResource(resId);
        }

        // 4. 事件监听
        btnPauseResume.setOnClickListener(v -> togglePauseResume());
        btnExit.setOnClickListener(v -> showExitConfirmationDialog());

        // 5. 开启计时器和锁定
        startTimer(remainingMillis);
        new Handler(Looper.getMainLooper()).postDelayed(this::startLockTask, 500);

        // 6.启动呼吸动画：缩放 + 透明度变化
        initBreathingAnimation();
    }

    private void showExitConfirmationDialog() {
        // 如果当前没暂停，先自动暂停计时和动画
        boolean wasPaused = isPaused;
        if (!wasPaused) {
            togglePauseResume();
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("确定要退出吗？")
                .setMessage("如果现在退出，此次专注记录将不会被计入总时间。")
                .setPositiveButton("确定退出 (5s)", (d, which) -> {
                    // 关键修复：取消并置空，切断生命周期拦截
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    if (breathingAnimator != null) {
                        breathingAnimator.cancel();
                    }
                    try {
                        stopLockTask();
                    } catch (Exception e) {
                    }
                    finish(); // 现在 finish() 不会再触发 onPause 中的自动重启了
                })
                .setNegativeButton("取消", (d, which) -> {
                    if (!wasPaused) togglePauseResume();
                })
                .setCancelable(false)
                .create();

        dialog.show();

        // 处理 5 秒冷静期
        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        positiveButton.setEnabled(false); // 初始禁用

        new CountDownTimer(5000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                positiveButton.setText("确定退出 (" + (millisUntilFinished / 1000 + 1) + "s)");
            }

            @Override
            public void onFinish() {
                positiveButton.setEnabled(true);
                positiveButton.setText("确定退出");
            }
        }.start();
    }

    private void initBreathingAnimation() {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(tvCountdown, "scaleX", 1.0f, 1.05f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(tvCountdown, "scaleY", 1.0f, 1.05f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(tvCountdown, "alpha", 1.0f, 0.7f);

        breathingAnimator = new AnimatorSet();
        breathingAnimator.playTogether(scaleX, scaleY, alpha);
        breathingAnimator.setDuration(2000);
        breathingAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        // 设置无限循环
        scaleX.setRepeatCount(ObjectAnimator.INFINITE);
        scaleX.setRepeatMode(ObjectAnimator.REVERSE);
        scaleY.setRepeatCount(ObjectAnimator.INFINITE);
        scaleY.setRepeatMode(ObjectAnimator.REVERSE);
        alpha.setRepeatCount(ObjectAnimator.INFINITE);
        alpha.setRepeatMode(ObjectAnimator.REVERSE);

        breathingAnimator.start();
    }


    private void togglePauseResume() {
        if (isPaused) {
            startTimer(remainingMillis);
            btnPauseResume.setImageResource(android.R.drawable.ic_media_pause);
            if (breathingAnimator != null) breathingAnimator.resume(); // 恢复动画
            isPaused = false;
        } else {
            if (timer != null) timer.cancel();
            btnPauseResume.setImageResource(android.R.drawable.ic_media_play);
            if (breathingAnimator != null) breathingAnimator.pause(); // 暂停动画
            isPaused = true;
        }
    }

    private void startTimer(long millis) {
        timer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long left) {
                // 实时更新剩余时间
                remainingMillis = left;

                int h = (int) (left / 3600000);
                int m = (int) (left % 3600000 / 60000);
                int s = (int) (left % 60000 / 1000);
                tvCountdown.setText(String.format("%02d:%02d:%02d", h, m, s));

                // 更新进度条
                progressView.setProgress((left / (float) totalTime) * 100f);
            }

            @Override
            public void onFinish() {
                // 如果已经手动退出了（timer 为空），则不执行自然结束逻辑
                if (timer == null) return;

                remainingMillis = 0;
                timer = null; // 正常结束也置空

                int minutes = getIntent().getIntExtra("minutes", 0);
                if (breathingAnimator != null) breathingAnimator.end();

                repository.addFocusRecord(minutes);

                String today = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

                new Thread(() -> {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    long totalMinutes = repository.getTodayTotalMinutes(today);

                    // 关键修复：检查 Activity 是否正在销毁或已结束
                    runOnUiThread(() -> {
                        if (!isFinishing() && !isDestroyed()) {
                            showSuccessDialog(minutes, totalMinutes);
                        }
                    });
                }).start();
            }
        }.start();


    }

    private void showSuccessDialog(int currentMinutes, long totalMinutes) {
        // 停止锁定模式，允许用户操作
        try {
            stopLockTask();
        } catch (Exception e) {
        }

        // 创建自定义布局对话框
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_focus_success, null);
        builder.setView(view);

        TextView tvMessage = view.findViewById(R.id.tvSuccessMessage);
        Button btnConfirm = view.findViewById(R.id.btnSuccessConfirm);

        // 设置鼓励内容
        String message = "本次专注：" + currentMinutes + " 分钟\n今日总专注时间：" + totalMinutes + " 分钟";
        tvMessage.setText(message);

        AlertDialog dialog = builder.create();
        // 设置点击外部不消失，强制用户看一眼成就感
        dialog.setCancelable(false);

        // 让弹窗背景变成圆角（需要配合代码设置透明背景）
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        btnConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            finish(); // 彻底结束活动，返回主界面
        });

        dialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 只有在 timer 还在运行且没有点击“暂停”的情况下，才认为是恶意逃逸
        if (timer != null && !isPaused) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // 如果在这 500ms 内 timer 没被置空，说明确实需要拦截
                if (timer != null) {
                    Intent intent = new Intent(this, FocusLockActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                            Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                }
            }, 500);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 试图逃逸返回时，立即重新锁定
        if (timer != null) {
            new Handler(Looper.getMainLooper()).postDelayed(this::startLockTask, 500);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (timer != null) {
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