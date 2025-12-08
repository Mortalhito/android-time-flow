package com.example.timeflow.entity;

import java.util.ArrayList;
import java.util.List;

public class Habit {
    private String name;
    private String id;
    private int totalDays;
    private int completedDays;
    private boolean completedToday;
    private List<Boolean> weeklyCompletion; // 本周完成情况
    private List<Boolean> monthlyCompletion; // 本月完成情况

    public Habit(String name, int totalDays, int completedDays) {
        this.name = name;
        this.totalDays = totalDays;
        this.completedDays = completedDays;
        this.completedToday = false;
        this.id = String.valueOf(System.currentTimeMillis());
        this.weeklyCompletion = new ArrayList<>();
        this.monthlyCompletion = new ArrayList<>();
        initializeCompletionArrays();
    }

    private void initializeCompletionArrays() {
        // 初始化本周完成数组（7天）
        for (int i = 0; i < 7; i++) {
            weeklyCompletion.add(false);
        }
        // 初始化本月完成数组（30天）
        for (int i = 0; i < 30; i++) {
            monthlyCompletion.add(false);
        }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public int getCompletedDays() { return completedDays; }
    public void setCompletedDays(int completedDays) { this.completedDays = completedDays; }

    public boolean isCompletedToday() { return completedToday; }
    public void setCompletedToday(boolean completedToday) {
        this.completedToday = completedToday;
        if (completedToday) {
            // 更新完成天数
            this.completedDays++;
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<Boolean> getWeeklyCompletion() { return weeklyCompletion; }
    public List<Boolean> getMonthlyCompletion() { return monthlyCompletion; }

    public float getWeeklyCompletionRate() {
        int completed = 0;
        for (Boolean completedDay : weeklyCompletion) {
            if (completedDay) completed++;
        }
        return weeklyCompletion.size() > 0 ? (float) completed / weeklyCompletion.size() * 100 : 0;
    }

    public float getMonthlyCompletionRate() {
        int completed = 0;
        for (Boolean completedDay : monthlyCompletion) {
            if (completedDay) completed++;
        }
        return monthlyCompletion.size() > 0 ? (float) completed / monthlyCompletion.size() * 100 : 0;
    }
}