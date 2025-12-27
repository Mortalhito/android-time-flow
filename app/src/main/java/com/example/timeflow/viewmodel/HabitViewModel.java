package com.example.timeflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.room.entity.HabitRecord;
import com.example.timeflow.room.entity.HabitWithStats;
import com.example.timeflow.room.repository.HabitRepository;

import java.util.List;

public class HabitViewModel extends AndroidViewModel {
    private final HabitRepository repository;
    private final LiveData<List<Habit>> allHabits;
    private MutableLiveData<HabitRecord> currentRecord = new MutableLiveData<>();

    public HabitViewModel(@NonNull Application application) {
        super(application);
        repository = new HabitRepository(application);
        allHabits = repository.getAllHabits();
    }


    public void insert(Habit habit) {
        repository.insert(habit);
    }

    public void update(Habit habit) {
        repository.update(habit);
    }

    public void delete(Habit habit) {
        repository.delete(habit);
    }

    public void checkIn(long habitId) {
        // 只在需要时创建记录
        repository.getOrCreateTodayRecord(habitId, record -> {
            if (record.getCompletedCount() < 1) {
                record.setCompletedCount(1);
                repository.updateRecord(record);
            }
        });
    }

    public LiveData<List<HabitWithStats>> getAllHabitsWithStats() {
        return repository.getAllHabitsWithStats();
    }
}