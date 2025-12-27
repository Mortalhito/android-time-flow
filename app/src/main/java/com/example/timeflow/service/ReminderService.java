package com.example.timeflow.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.timeflow.room.entity.CalendarEvent;

import java.util.Calendar;
import java.util.Date;

public class ReminderService {

    public static void scheduleReminder(Context context, CalendarEvent event) {
        Log.d("ReminderDebug", "=== 开始设置提醒 ===");
        Log.d("ReminderDebug", "事件ID: " + event.getId());
        Log.d("ReminderDebug", "事件标题: " + event.getTitle());
        Log.d("ReminderDebug", "提醒时间: " + event.getReminderTime() + "分钟");
        Log.d("ReminderDebug", "提醒启用: " + event.isReminderEnabled());

        if (!event.isReminderEnabled()) {
            Log.d("ReminderService", "提醒未启用");
            return;
        }

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e("ReminderService", "无法获取 AlarmManager");
                return;
            }

            String action = "ACTION_EVENT_REMINDER_" + event.getId();
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.setAction(action);
            intent.putExtra("event_title", event.getTitle());
            intent.putExtra("event_time", event.getTime());

            int requestCode = Math.abs(event.getId().hashCode()); // 确保为正数
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            long reminderTime = calculateReminderTime(event);
            Log.d("ReminderService", "设置提醒: " + event.getTitle() +
                    ", 提醒时间: " + new Date(reminderTime) +
                    ", 当前时间: " + new Date() +
                    ", 时间差: " + (reminderTime - System.currentTimeMillis()) + "ms");

            if (reminderTime > System.currentTimeMillis()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
                }
                Log.d("ReminderService", "提醒设置成功");
            } else {
                Log.w("ReminderService", "提醒时间已过，不设置提醒");
                // 取消已过期的提醒
                cancelReminder(context, event);
            }
        } catch (Exception e) {
            Log.e("ReminderService", "设置提醒失败", e);
        }
    }

    public static void cancelReminder(Context context, CalendarEvent event) {
        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e("ReminderService", "无法获取 AlarmManager");
                return;
            }

            String action = "ACTION_EVENT_REMINDER_" + event.getId();
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.setAction(action); // 必须与设置时保持一致

            int requestCode = Math.abs(event.getId().hashCode());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel(); // 额外取消 PendingIntent

            Log.d("ReminderService", "提醒已取消: " + event.getTitle());
        } catch (Exception e) {
            Log.e("ReminderService", "取消提醒失败", e);
        }
    }

    private static long calculateReminderTime(CalendarEvent event) {
        try {
            if (event.getDate() == null || event.getTime() == null) {
                throw new IllegalArgumentException("日期或时间为空");
            }

            String[] dateParts = event.getDate().split("-");
            String[] timeParts = event.getTime().split(":");

            if (dateParts.length != 3 || timeParts.length < 2) {
                throw new IllegalArgumentException("日期时间格式错误");
            }

            int year = Integer.parseInt(dateParts[0]);
            int month = Integer.parseInt(dateParts[1]) - 1; // Calendar 月份从0开始
            int day = Integer.parseInt(dateParts[2]);
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            Calendar eventTime = Calendar.getInstance();
            eventTime.set(year, month, day, hour, minute, 0);
            eventTime.set(Calendar.MILLISECOND, 0);

            // 验证日期是否合理
            if (eventTime.get(Calendar.YEAR) != year ||
                    eventTime.get(Calendar.MONTH) != month ||
                    eventTime.get(Calendar.DAY_OF_MONTH) != day) {
                throw new IllegalArgumentException("无效的日期");
            }

            int offsetMinutes;
            try {
                offsetMinutes = Integer.parseInt(event.getReminderTime());
            } catch (NumberFormatException e) {
                offsetMinutes = 0;
                Log.w("ReminderService", "提醒时间格式错误，使用默认值0");
            }

            eventTime.add(Calendar.MINUTE, -offsetMinutes);
            return eventTime.getTimeInMillis();

        } catch (Exception e) {
            Log.e("ReminderService", "计算提醒时间错误: " + e.getMessage());
            // 返回一个明显错误的时间，便于调试
            return System.currentTimeMillis() + 60000; // 1分钟后
        }
    }
}