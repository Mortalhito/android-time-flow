package com.example.timeflow.room.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CountdownEvent {
    private String name;
    private String targetDate;
    private int daysLeft;
    private String id;
    private boolean isPast;
    private String category;
    private int categoryId;
    private String categoryName;
    private int categoryColor;

    public CountdownEvent(String name, int categoryId, String targetDate) {
        this.name = name;
        this.categoryId = categoryId;
        this.targetDate = targetDate;
        this.id = String.valueOf(System.currentTimeMillis()); // 临时ID
        calculateDaysLeft(); // 重要：必须调用！
    }

    public CountdownEvent() {
        this.id = String.valueOf(System.currentTimeMillis()); // 默认ID
    }

    // 修改 getDisplayText() 方法，确保不会返回 null
    public String getDisplayText() {
        calculateDaysLeft(); // 确保每次显示时都计算最新天数

        int absoluteDays = Math.abs(daysLeft);
        if (isPast) {
            return "已经" + absoluteDays + "天";
        } else {
            return "还有" + absoluteDays + "天";
        }
    }

    // 确保 calculateDaysLeft() 总是能正确计算
    public void calculateDaysLeft() {
        if (targetDate == null || targetDate.isEmpty()) {
            this.daysLeft = 0;
            this.isPast = false;
            return;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date target = sdf.parse(targetDate);
            Date today = new Date();

            long diff = target.getTime() - today.getTime();
            this.daysLeft = (int) (diff / (1000 * 60 * 60 * 24));
            this.isPast = diff < 0;
        } catch (ParseException e) {
            e.printStackTrace();
            this.daysLeft = 0;
            this.isPast = false;
        }
    }

    // 添加一个安全的方法来获取事件名称
    public String getSafeName() {
        return name != null ? name : "未命名事件";
    }

    // Getters and Setters 保持不变
    public String getName() {
        return name != null ? name : "未命名事件";
    }

    public void setName(String name) {
        this.name = name;
        calculateDaysLeft(); // 名称改变时重新计算
    }

    public String getTargetDate() {
        return targetDate != null ? targetDate : "";
    }

    public void setTargetDate(String targetDate) {
        this.targetDate = targetDate;
        calculateDaysLeft(); // 日期改变时重新计算
    }

    // 其他 getter/setter 方法保持不变...
    public int getDaysLeft() {
        calculateDaysLeft(); // 确保返回最新值
        return daysLeft;
    }

    public void setDaysLeft(int daysLeft) {
        this.daysLeft = daysLeft;
    }

    public String getId() {
        return id != null ? id : String.valueOf(System.currentTimeMillis());
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isPast() {
        calculateDaysLeft(); // 确保返回最新值
        return isPast;
    }

    public void setPast(boolean past) {
        isPast = past;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName != null ? categoryName : "未分类";
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategory() {
        return category != null ? category : "未分类";
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCategoryColor() {
        return categoryColor;
    }

    public void setCategoryColor(int categoryColor) {
        this.categoryColor = categoryColor;
    }
}