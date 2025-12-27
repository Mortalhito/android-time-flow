package com.example.timeflow.room.entity;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.timeflow.utils.Converters;

import java.util.Date;

@Entity(
        tableName = "habit_records",
        foreignKeys = @ForeignKey(
                entity = Habit.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("habitId"), @Index("completionDate")}
)
@TypeConverters({Converters.class})
public class HabitRecord {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private Long habitId; // 外键，关联Habit
    private Date completionDate; // 完成日期
    private int completedCount; // 当天完成次数

    @Ignore
    public HabitRecord() {}

    public HabitRecord(Long habitId, Date completionDate, int completedCount) {
        this.habitId = habitId;
        this.completionDate = completionDate;
        this.completedCount = completedCount;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHabitId() { return habitId; }
    public void setHabitId(Long habitId) { this.habitId = habitId; }

    public Date getCompletionDate() { return completionDate; }
    public void setCompletionDate(Date completionDate) { this.completionDate = completionDate; }

    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
}