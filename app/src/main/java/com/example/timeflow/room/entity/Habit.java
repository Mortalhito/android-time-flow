package com.example.timeflow.room.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timeflow.utils.Converters;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity(tableName = "habits")
@TypeConverters({Converters.class})
public class Habit {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String name;
    private Date startTime;
    private Date endTime; // null表示无限期
    private String tip; // 备注/鼓励的话
    private boolean random; // true表示周随机，false表示周定期
    private List<Integer> days; // 频次设置

    public Habit() {
        this.days = new ArrayList<>();
    }

    @Ignore
    public Habit(String name, Date startTime, Date endTime, String tip, boolean random, List<Integer> days) {
        this();
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tip = tip;
        this.random = random;
        this.days = days != null ? days : new ArrayList<>();
    }

    // 检查今天是否需要完成
    public boolean isTodayInFrequency() {
        if (days == null || days.isEmpty()) return true;

        if (random) {
            // 周随机模式：只要还没到周结束，都应该显示（由用户决定哪天完成）
            return true;
        }

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK);
        return days.contains(today);
    }

    // 获取每周目标天数
    public int getWeeklyTargetDays() {
        if (random) {
            // 周随机：days中第一个元素表示每周随机完成几天
            return days.isEmpty() ? 0 : days.get(0);
        } else {
            // 周定期：days的大小表示每周固定完成几天
            return days.size();
        }
    }

    // 新增：判断习惯今天是否应该显示在未完成区
    public boolean shouldShowInUncompleted() {
        return isTodayInFrequency();
        // 完成状态由数据库查询决定，这里只判断频率
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }

    public boolean isRandom() { return random; }
    public void setRandom(boolean random) { this.random = random; }

    public List<Integer> getDays() { return days; }
    public void setDays(List<Integer> days) { this.days = days; }
}