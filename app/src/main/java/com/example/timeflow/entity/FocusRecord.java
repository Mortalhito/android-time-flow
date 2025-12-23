package com.example.timeflow.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "focus_records")
public class FocusRecord {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;          // 格式: 2025-12-23
    public String time;          // 格式: 14:30:05
    public int focusMinutes;     // 专注时长

    // 预留给服务器同步功能
    public boolean isSynced = false;
    public String userId = "local_user";

    public FocusRecord(String date, String time, int focusMinutes) {
        this.date = date;
        this.time = time;
        this.focusMinutes = focusMinutes;
    }
}