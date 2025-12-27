package com.example.timeflow.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.room.entity.HabitRecord;
import com.example.timeflow.room.entity.HabitWithStats;

import java.util.List;

@Dao
public interface HabitDao {
    // Habit相关操作
    @Insert
    long insert(Habit habit);

    @Update
    void update(Habit habit);

    @Delete
    void delete(Habit habit);

    @Query("SELECT * FROM habits ORDER BY id DESC")
    LiveData<List<Habit>> getAllHabits();

    @Query("SELECT * FROM habits WHERE id = :id")
    Habit getHabitById(long id);

    @Query("DELETE FROM habits")
    void deleteAll();

    // HabitRecord相关操作
    @Insert
    long insertRecord(HabitRecord record);

    @Update
    void updateRecord(HabitRecord record);

    @Delete
    void deleteRecord(HabitRecord record);

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId ORDER BY completionDate DESC")
    LiveData<List<HabitRecord>> getRecordsByHabitId(long habitId);

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND completionDate = :date")
    HabitRecord getRecordByHabitIdAndDate(long habitId, java.util.Date date);

    @Query("SELECT SUM(completedCount) FROM habit_records WHERE habitId = :habitId AND completedCount >= 1")
    LiveData<Integer> getTotalCompletedCount(long habitId);

    // 修改统计方法：统计有完成记录的天数（完成次数>=1）
    @Query("SELECT COUNT(DISTINCT DATE(completionDate)) FROM habit_records WHERE habitId = :habitId AND completedCount >= 1")
    LiveData<Integer> getTotalCompletedDays(long habitId);

    @Query("SELECT * FROM habit_records WHERE habitId = :habitId AND completionDate = :date AND completedCount >= 1")
    HabitRecord getTodayCompletedRecord(long habitId, java.util.Date date);

    @Query("SELECT h.*, " +
            "(SELECT COUNT(DISTINCT DATE(completionDate/1000, 'unixepoch')) " +
            " FROM habit_records r WHERE r.habitId = h.id AND r.completedCount >= 1) as totalDays, " +
            "(EXISTS(SELECT 1 FROM habit_records r " +
            " WHERE r.habitId = h.id " +
            " AND DATE(completionDate/1000, 'unixepoch', 'localtime') = DATE('now', 'localtime') " +
            " AND r.completedCount >= 1)) as isCompletedToday " +
            "FROM habits h ORDER BY h.id DESC")
    LiveData<List<HabitWithStats>> getAllHabitsWithStats();
}