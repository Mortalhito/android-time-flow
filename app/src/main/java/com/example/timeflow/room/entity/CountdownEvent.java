package com.example.timeflow.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;
import androidx.room.Ignore;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "countdown_events")
public class CountdownEvent {
    @PrimaryKey
    @NonNull
    private String id;
    private String name;
    @ColumnInfo(name = "category_id")
    private int categoryId;
    @ColumnInfo(name = "target_date")
    private String targetDate;

    @Ignore
    private int daysLeft;
    @Ignore
    private boolean isPast;
    @Ignore
    private String categoryName;
    @Ignore
    private int categoryColor;

    @Ignore
    public CountdownEvent() {
        this.id = String.valueOf(System.currentTimeMillis());
    }

    public CountdownEvent(String name, int categoryId, String targetDate) {
        this.id = String.valueOf(System.currentTimeMillis());
        this.name = name;
        this.categoryId = categoryId;
        this.targetDate = targetDate;
    }

    public void calculateDaysLeft() {
        if (targetDate == null || targetDate.isEmpty()) return;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date target = sdf.parse(targetDate);
            Date today = new Date();
            long diff = target.getTime() - today.getTime();
            this.daysLeft = (int) (diff / (1000 * 60 * 60 * 24));
            this.isPast = diff < 0;
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String getDisplayText() {
        calculateDaysLeft();
        int absDays = Math.abs(daysLeft);
        return isPast ? "已经" + absDays + "天" : "还有" + absDays + "天";
    }

    @NonNull public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getTargetDate() { return targetDate; }
    public void setTargetDate(String targetDate) { this.targetDate = targetDate; }
    public int getDaysLeft() { return daysLeft; }
    public void setDaysLeft(int daysLeft) { this.daysLeft = daysLeft; }
    public boolean isPast() { return isPast; }
    public void setPast(boolean past) { isPast = past; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public int getCategoryColor() { return categoryColor; }
    public void setCategoryColor(int categoryColor) { this.categoryColor = categoryColor; }
}