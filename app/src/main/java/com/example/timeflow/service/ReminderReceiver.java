package com.example.timeflow.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.timeflow.MainActivity;
import com.example.timeflow.R;

public class ReminderReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "event_reminder_channel";
    private static final String CHANNEL_NAME = "事务提醒";
    private static final String CHANNEL_DESCRIPTION = "事务提醒通知";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String eventTitle = intent.getStringExtra("event_title");
            String eventTime = intent.getStringExtra("event_time");

            if (eventTitle == null || eventTime == null) {
                Log.e("ReminderReceiver", "事件标题或时间为空");
                return;
            }

            // 创建通知渠道（Android 8.0+）
            createNotificationChannel(context);

            // 创建点击通知后的意图
            Intent mainIntent = new Intent(context, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    mainIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            // 构建通知
            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.notify)
                    .setContentTitle("事务提醒")
                    .setContentText(eventTitle + " (" + eventTime + ")")
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build();

            // 显示通知
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.notify(eventTitle.hashCode(), notification);
            }

            Log.d("ReminderReceiver", "提醒已发送: " + eventTitle + " at " + eventTime);

        } catch (Exception e) {
            Log.e("ReminderReceiver", "发送提醒失败", e);
        }
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager =
                    context.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
