package com.example.timeflow.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CountdownEvent {
    private String name;
    private String category;
    private String targetDate;
    private int daysLeft;
    private String id;

    public CountdownEvent(String name, String category, String targetDate, int daysLeft) {
        this.name = name;
        this.category = category;
        this.targetDate = targetDate;
        this.daysLeft = daysLeft;
        this.id = String.valueOf(System.currentTimeMillis());
        calculateDaysLeft();
    }

    public CountdownEvent() {}

    private void calculateDaysLeft() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date target = sdf.parse(targetDate);
            Date today = new Date();

            long diff = target.getTime() - today.getTime();
            this.daysLeft = (int) (diff / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            e.printStackTrace();
            this.daysLeft = 0;
        }
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        calculateDaysLeft();
    }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
        calculateDaysLeft();
    }

    public int getDaysLeft() {
        calculateDaysLeft(); // 每次获取时重新计算
        return daysLeft;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
}