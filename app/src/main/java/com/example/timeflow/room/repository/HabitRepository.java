package com.example.timeflow.room.repository;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.example.timeflow.room.dao.HabitDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.room.entity.HabitRecord;
import com.example.timeflow.room.entity.HabitWithStats;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {
    private HabitDao habitDao;
    private ExecutorService executorService;
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());
    public HabitRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        habitDao = database.habitDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    // Habit相关操作
    public LiveData<List<Habit>> getAllHabits() {
        return habitDao.getAllHabits();
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

    // HabitRecord相关操作
    public void insertRecord(HabitRecord record) {
        executorService.execute(() -> habitDao.insertRecord(record));
    }

    public void updateRecord(HabitRecord record) {
        executorService.execute(() -> habitDao.updateRecord(record));
    }

    public LiveData<List<HabitRecord>> getRecordsByHabitId(long habitId) {
        return habitDao.getRecordsByHabitId(habitId);
    }

    public LiveData<Integer> getTotalCompletedCount(long habitId) {
        return habitDao.getTotalCompletedCount(habitId);
    }

    public LiveData<Integer> getTotalCompletedDays(long habitId) {
        return habitDao.getTotalCompletedDays(habitId);
    }

    // 获取或创建今天的记录


    // 新增：检查习惯今天是否已经完成
    public void checkTodayCompleted(long habitId, OnTodayCompletedCallback callback) {
        executorService.execute(() -> {
            Date today = new Date();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(today);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            Date todayDate = cal.getTime();

            HabitRecord record = habitDao.getTodayCompletedRecord(habitId, todayDate);
            boolean isCompleted = record != null;

            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                callback.onTodayChecked(isCompleted);
            });
        });
    }

    public interface OnTodayCompletedCallback {
        void onTodayChecked(boolean isCompleted);
    }

    public interface OnRecordReadyCallback {
        void onRecordReady(HabitRecord record);
    }

    public LiveData<List<HabitWithStats>> getAllHabitsWithStats() {
        return habitDao.getAllHabitsWithStats();
    }


    public void getOrCreateTodayRecord(long habitId, OnRecordReadyCallback callback) {
        executorService.execute(() -> {
            Date todayDate = getTodayDate(); // 提取为方法

            HabitRecord record = habitDao.getTodayCompletedRecord(habitId, todayDate);

            if (record == null) {
                record = new HabitRecord(habitId, todayDate, 0); // 初始为0
                long id = habitDao.insertRecord(record);
                record.setId(id);
            }

            HabitRecord finalRecord = record;
            mainHandler.post(() -> callback.onRecordReady(finalRecord));
        });
    }

    private Date getTodayDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
}