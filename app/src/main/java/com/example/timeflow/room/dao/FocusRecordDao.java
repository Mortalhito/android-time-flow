package com.example.timeflow.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.timeflow.room.entity.FocusRecord;
import java.util.List;

@Dao
public interface FocusRecordDao {
    @Insert
    void insert(FocusRecord record);

    // 查询指定日期的专注总分钟数
    @Query("SELECT SUM(focusMinutes) FROM focus_records WHERE date = :dateLimit")
    long getTotalMinutesByDate(String dateLimit);

    // 获取所有记录（用于以后展示历史列表）
    @Query("SELECT * FROM focus_records ORDER BY id DESC")
    List<FocusRecord> getAllRecords();
}