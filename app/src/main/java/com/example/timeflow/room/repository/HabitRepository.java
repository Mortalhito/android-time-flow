package com.example.timeflow.room.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;

import com.example.timeflow.room.dao.HabitDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.room.entity.HabitRecord;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRepository {
    private HabitDao habitDao;
    private ExecutorService executorService;

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
    public void getOrCreateTodayRecord(long habitId, OnRecordReadyCallback callback) {
        executorService.execute(() -> {
            // 获取今天的日期（去掉时间部分）
            Date today = new Date();
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(today);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            Date todayDate = cal.getTime();

            HabitRecord record = habitDao.getRecordByHabitIdAndDate(habitId, todayDate);
            if (record == null) {
                record = new HabitRecord(habitId, todayDate, 0);
                long recordId = habitDao.insertRecord(record);
                record.setId(recordId);
            }

            HabitRecord finalRecord = record;
            // 回到主线程回调
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                callback.onRecordReady(finalRecord);
            });
        });
    }

    public interface OnRecordReadyCallback {
        void onRecordReady(HabitRecord record);
    }
}