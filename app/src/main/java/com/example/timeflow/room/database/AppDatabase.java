package com.example.timeflow.room.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.timeflow.room.dao.CalendarEventDao;
import com.example.timeflow.room.dao.FocusRecordDao;
import com.example.timeflow.room.dao.HabitDao;
import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.entity.CalendarEvent;
import com.example.timeflow.room.entity.FocusRecord;
import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.room.entity.User;
import com.example.timeflow.utils.Converters;

@Database(entities = {CalendarEvent.class, Habit.class, FocusRecord.class, User.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract CalendarEventDao calendarEventDao();
    public abstract HabitDao habitDao();
    public abstract FocusRecordDao focusRecordDao();
    public abstract UserDao userDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "timeflow.db"
                            )
                            .fallbackToDestructiveMigration() // 开发阶段使用，正式环境应编写Migration
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}