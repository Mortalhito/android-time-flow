package com.example.timeflow.entity;


import com.example.timeflow.R;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CalendarEvent implements Serializable {

    private static final long serialVersionUID = 1L; // 添加序列化版本UID
    private String title;
    private String date;
    private String priority; // "high", "medium", "low"
    private String time;
    private String description;
    private String id;
    private boolean reminderEnabled;
    private String reminderTime;

    public CalendarEvent(String title, String date, String priority, String time) {
        this.title = title;
        this.date = date;
        this.priority = priority;
        this.time = time;
        this.id = String.valueOf(System.currentTimeMillis());
        this.reminderEnabled = false;
        this.reminderTime = time;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public boolean isReminderEnabled() { return reminderEnabled; }
    public void setReminderEnabled(boolean reminderEnabled) { this.reminderEnabled = reminderEnabled; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public int getPriorityColor() {
        switch (priority) {
            case "high": return R.color.red;
            case "medium": return R.color.yellow;
            case "low": return R.color.green;
            default: return R.color.green;
        }
    }

    public String getFormattedDateTime() {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
            Date dateObj = inputFormat.parse(date + " " + time);
            return outputFormat.format(dateObj);
        } catch (Exception e) {
            return date + " " + time;
        }
    }
}