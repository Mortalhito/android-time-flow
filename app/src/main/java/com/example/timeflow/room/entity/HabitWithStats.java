package com.example.timeflow.room.entity;

import androidx.room.Embedded;

// HabitWithStats.java
public class HabitWithStats {
    @Embedded
    public Habit habit;

    public int totalDays; // 总坚持天数
    public boolean isCompletedToday; // 今天是否已打卡
}