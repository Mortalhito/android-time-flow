package com.example.timeflow.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.timeflow.dao.CalendarEventDao;
import com.example.timeflow.dao.HabitDao;
import com.example.timeflow.entity.CalendarEvent;
import com.example.timeflow.entity.Habit;
import com.example.timeflow.utils.Converters;

@Database(entities = {CalendarEvent.class, Habit.class}, version = 2, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract CalendarEventDao calendarEventDao();
    public abstract HabitDao habitDao();

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