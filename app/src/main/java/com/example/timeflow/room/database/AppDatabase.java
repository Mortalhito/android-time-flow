package com.example.timeflow.room.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.timeflow.room.dao.CalendarEventDao;
import com.example.timeflow.room.dao.CategoryDao;
import com.example.timeflow.room.dao.CountdownEventDao;
import com.example.timeflow.room.dao.FocusRecordDao;
import com.example.timeflow.room.dao.HabitDao;
import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.entity.CalendarEvent;
import com.example.timeflow.room.entity.Category;
import com.example.timeflow.room.entity.CountdownEvent;
import com.example.timeflow.room.entity.FocusRecord;
import com.example.timeflow.room.entity.Habit;
import com.example.timeflow.room.entity.User;
import com.example.timeflow.utils.Converters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {CalendarEvent.class, Habit.class, FocusRecord.class, User.class, Category.class, CountdownEvent.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract CalendarEventDao calendarEventDao();
    public abstract HabitDao habitDao();
    public abstract FocusRecordDao focusRecordDao();
    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();
    public abstract CountdownEventDao eventDao();

    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(4);

    public static AppDatabase getInstance(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "countdown_room_new.db")
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    databaseWriteExecutor.execute(() -> {
                                        fillDefaultData(INSTANCE);
                                    });
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    private static void fillDefaultData(AppDatabase db) {
        CategoryDao dao = db.categoryDao();
        dao.insert(new Category("生活", 0xFF42A5F5, true));
        dao.insert(new Category("工作", 0xFF66BB6A, true));
        dao.insert(new Category("纪念日", 0xFFFFA726, true));
    }


}