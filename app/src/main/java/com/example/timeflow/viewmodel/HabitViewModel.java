package com.example.timeflow.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.room.entity.HabitRecord;
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

    public LiveData<List<Habit>> getAllHabits() {
        return allHabits;
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

    // HabitRecord相关方法
    public void getOrCreateTodayRecord(long habitId) {
        repository.getOrCreateTodayRecord(habitId, new HabitRepository.OnRecordReadyCallback() {
            @Override
            public void onRecordReady(HabitRecord record) {
                currentRecord.setValue(record);
            }
        });
    }

    public LiveData<HabitRecord> getCurrentRecord() {
        return currentRecord;
    }

    public void updateRecord(HabitRecord record) {
        repository.updateRecord(record);
    }

    public LiveData<List<HabitRecord>> getRecordsByHabitId(long habitId) {
        return repository.getRecordsByHabitId(habitId);
    }

    public LiveData<Integer> getTotalCompletedCount(long habitId) {
        return repository.getTotalCompletedCount(habitId);
    }

    public LiveData<Integer> getTotalCompletedDays(long habitId) {
        return repository.getTotalCompletedDays(habitId);
    }
}