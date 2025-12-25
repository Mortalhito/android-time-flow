package com.example.timeflow.room.repository;

import android.app.Application;

import com.example.timeflow.room.dao.FocusRecordDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.FocusRecord;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FocusRecordRepository {
    private FocusRecordDao focusRecordDao;
    // 使用线程池处理异步操作，比 new Thread 更高效
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public FocusRecordRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        focusRecordDao = db.focusRecordDao();
    }

    // 提供给外部的简单接口：保存一次专注
    public void addFocusRecord(int minutes) {
        executorService.execute(() -> {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            String time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

            FocusRecord record = new FocusRecord(date, time, minutes);
            focusRecordDao.insert(record);
        });
    }

    // 供外部调用，返回指定日期的总分钟数
    public long getTodayTotalMinutes(String date) {
        return focusRecordDao.getTotalMinutesByDate(date);
    }

    // 以后你接入网络时，只需要在这里增加网络同步的逻辑
    // ViewModel 完全不需要改动
}