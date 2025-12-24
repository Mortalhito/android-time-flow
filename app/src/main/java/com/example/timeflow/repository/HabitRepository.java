package com.example.timeflow.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.timeflow.room.dao.HabitDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.Habit;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {
    private HabitDao habitDao;
    private LiveData<List<Habit>> allHabits;
    private ExecutorService executorService;

    public HabitRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        habitDao = database.habitDao();
        allHabits = habitDao.getAllHabits();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
    }

    public void insert(Habit habit) {
        executorService.execute(() -> habitDao.insert(habit));
    }

    public void update(Habit habit) {
        executorService.execute(() -> habitDao.update(habit));
    }

    public void delete(Habit habit) {
        executorService.execute(() -> habitDao.delete(habit));
    }
}