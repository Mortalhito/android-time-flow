package com.example.timeflow.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timeflow.utils.Converters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity(tableName = "habits")
@TypeConverters({Converters.class})
public class Habit {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name;
    private int totalDays;
    private int completedDays;
    private boolean completedToday;
    private List<Boolean> weeklyCompletion;
    private List<Boolean> monthlyCompletion;

    // 新增字段
    private List<Integer> frequency; // 频次：存储选中的星期几（1-7，1=周日，7=周六）
    private Date startDate; // 开始时间
    private Date endDate; // 结束时间（null表示无限期）
    private int dailyTargetCount; // 每天目标次数
    private int todayCompletedCount; // 今天已完成次数



    public Habit() {
        this.weeklyCompletion = new ArrayList<>();
        this.monthlyCompletion = new ArrayList<>();
        this.frequency = new ArrayList<>();
        initializeCompletionArrays();
    }

    @Ignore
    public Habit(String name, int totalDays, int completedDays) {
        this();
        this.name = name;
        this.totalDays = totalDays;
        this.completedDays = completedDays;
        this.startDate = new Date(); // 默认开始时间为今天
        this.dailyTargetCount = 1; // 默认每天1次
        this.todayCompletedCount = 0;
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
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTotalDays() { return totalDays; }
    public void setTotalDays(int totalDays) { this.totalDays = totalDays; }

    public int getCompletedDays() { return completedDays; }
    public void setCompletedDays(int completedDays) { this.completedDays = completedDays; }

    public boolean isCompletedToday() { return completedToday; }
    public void setCompletedToday(boolean completed) {
        this.completedToday = completed;
        if (!completed) {
            // 重置今日计数但不减少总完成天数
            todayCompletedCount = 0;
        }
    }

    public List<Boolean> getWeeklyCompletion() { return weeklyCompletion; }
    public void setWeeklyCompletion(List<Boolean> weeklyCompletion) { this.weeklyCompletion = weeklyCompletion; }

    public List<Boolean> getMonthlyCompletion() { return monthlyCompletion; }
    public void setMonthlyCompletion(List<Boolean> monthlyCompletion) { this.monthlyCompletion = monthlyCompletion; }

    public List<Integer> getFrequency() { return frequency; }
    public void setFrequency(List<Integer> frequency) { this.frequency = frequency; }

    public Date getStartDate() { return startDate; }
    public void setStartDate(Date startDate) { this.startDate = startDate; }

    public Date getEndDate() { return endDate; }
    public void setEndDate(Date endDate) { this.endDate = endDate; }

    public int getDailyTargetCount() { return dailyTargetCount; }
    public void setDailyTargetCount(int dailyTargetCount) { this.dailyTargetCount = dailyTargetCount; }

    public int getTodayCompletedCount() { return todayCompletedCount; }
    public void setTodayCompletedCount(int todayCompletedCount) { this.todayCompletedCount = todayCompletedCount; }

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

    // 检查今天是否需要完成（根据频次设置）
    public boolean isTodayInFrequency() {
        if (frequency.isEmpty()) return true;

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int today = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        // 转换：Calendar中周日=1，周一=2，...，周六=7
        return frequency.contains(today);
    }

    // 增加今日完成次数
    public void incrementTodayCount() {
        // 添加边界检查
        if (todayCompletedCount < dailyTargetCount) {
            todayCompletedCount++;

            if (todayCompletedCount >= dailyTargetCount && !completedToday) {
                completedToday = true;
                completedDays++; // 只在第一次完成时增加总完成天数
            }
        }
    }
}