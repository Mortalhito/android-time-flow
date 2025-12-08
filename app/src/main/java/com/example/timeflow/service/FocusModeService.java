package com.example.timeflow.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.timeflow.R;

public class FocusModeService extends Service {
    private static final String CHANNEL_ID = "FocusModeChannel";
    private static final int NOTIFICATION_ID = 1;

    private CountDownTimer timer;
    private long totalTimeMillis;
    private long remainingTimeMillis;
    private FocusBinder binder = new FocusBinder();
    private FocusListener listener;

    public interface FocusListener {
        void onTimeUpdate(long remainingMillis);
        void onFocusFinished();
    }

    public class FocusBinder extends Binder {
        public FocusModeService getService() {
            return FocusModeService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @SuppressLint("ForegroundServiceType")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            int durationMinutes = intent.getIntExtra("duration_minutes", 25);
            totalTimeMillis = durationMinutes * 60 * 1000L;
            remainingTimeMillis = totalTimeMillis;

            startTimer();
            startForeground(NOTIFICATION_ID, createNotification());
        }
        return START_NOT_STICKY;
    }

    private void startTimer() {
        timer = new CountDownTimer(totalTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingTimeMillis = millisUntilFinished;
                if (listener != null) {
                    listener.onTimeUpdate(millisUntilFinished);
                }
                updateNotification();
            }

            @Override
            public void onFinish() {
                remainingTimeMillis = 0;
                if (listener != null) {
                    listener.onFocusFinished();
                }
                stopSelf();
            }
        }.start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "专注模式",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("专注模式运行中")
                .setContentText("专注时间剩余: " + formatTime(remainingTimeMillis))
                .setSmallIcon(R.drawable.ic_focus)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(NOTIFICATION_ID, createNotification());
    }

    private String formatTime(long millis) {
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void setFocusListener(FocusListener listener) {
        this.listener = listener;
    }

    public long getRemainingTime() {
        return remainingTimeMillis;
    }

    @Override
    public void onDestroy() {
        if (timer != null) {
            timer.cancel();
        }
        super.onDestroy();
    }
}