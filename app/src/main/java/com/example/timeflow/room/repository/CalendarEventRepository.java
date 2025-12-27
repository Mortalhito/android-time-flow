package com.example.timeflow.room.repository;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.timeflow.room.dao.CalendarEventDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.CalendarEvent;
import com.example.timeflow.service.ReminderService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarEventRepository {
    private CalendarEventDao eventDao;
    private ExecutorService databaseWriteExecutor;
    private Context context;
    private Handler mainHandler;

    public interface OperationListener {
        void onSuccess();
        void onError(Exception e);
    }

    public CalendarEventRepository(Context context) {
        this.context = context.getApplicationContext();
        AppDatabase database = AppDatabase.getInstance(this.context);
        eventDao = database.calendarEventDao();
        databaseWriteExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public void insertEvent(CalendarEvent event, OperationListener listener) {
        databaseWriteExecutor.execute(() -> {
            try {
                eventDao.insertEvent(event);
                // 设置提醒 - 修改条件判断
                if (event.isReminderEnabled() && event.getReminderTime() != null) {
                    ReminderService.scheduleReminder(context, event);
                }

                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public void updateEvent(CalendarEvent event, OperationListener listener) {
        databaseWriteExecutor.execute(() -> {
            try {
                // 取消旧提醒
                CalendarEvent oldEvent = eventDao.getEventById(event.getId());
                if (oldEvent != null) {
                    ReminderService.cancelReminder(context, oldEvent);
                }

                eventDao.updateEvent(event);

                // 设置新提醒
                if (event.isReminderEnabled() && event.getReminderTime() != null) {
                    ReminderService.scheduleReminder(context, event);
                }

                // 在主线程回调
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                });
            } catch (Exception e) {
                // 在主线程回调错误
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public void deleteEvent(CalendarEvent event, OperationListener listener) {
        databaseWriteExecutor.execute(() -> {
            try {
                // 取消提醒
                if (event.isReminderEnabled()) {
                    ReminderService.cancelReminder(context, event);
                }

                eventDao.deleteEvent(event);

                // 在主线程回调
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onSuccess();
                    }
                });
            } catch (Exception e) {
                // 在主线程回调错误
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public void getEventsByDate(String date, EventListListener listener) {
        databaseWriteExecutor.execute(() -> {
            try {
                List<CalendarEvent> events = eventDao.getEventsByDate(date);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onEventsLoaded(events);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public void getAllEvents(EventListListener listener) {
        databaseWriteExecutor.execute(() -> {
            try {
                List<CalendarEvent> events = eventDao.getAllEvents();
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onEventsLoaded(events);
                    }
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
            }
        });
    }

    public interface EventListListener {
        void onEventsLoaded(List<CalendarEvent> events);
        void onError(Exception e);
    }
}
