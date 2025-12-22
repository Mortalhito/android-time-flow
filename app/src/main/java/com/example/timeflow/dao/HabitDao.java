package com.example.timeflow.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.timeflow.entity.Habit;

import java.util.List;

@Dao
public interface HabitDao {
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
}