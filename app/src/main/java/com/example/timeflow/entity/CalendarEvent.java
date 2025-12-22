package com.example.timeflow.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@Entity(tableName = "calendar_events")
public class CalendarEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    @PrimaryKey
    @NotNull
    private String id;
    private String title;
    private String date;
    private String priority; // "high", "medium", "low"
    private String time;
    private String description;
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
        this.description = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }
}