package com.example.timeflow.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.room.entity.HabitRecord;

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

    @Query("SELECT SUM(completedCount) FROM habit_records WHERE habitId = :habitId")
    LiveData<Integer> getTotalCompletedCount(long habitId);

    @Query("SELECT COUNT(DISTINCT DATE(completionDate)) FROM habit_records WHERE habitId = :habitId")
    LiveData<Integer> getTotalCompletedDays(long habitId);
}