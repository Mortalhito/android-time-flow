package com.example.timeflow.repository;

import android.content.Context;

import com.example.timeflow.dao.AppDatabase;
import com.example.timeflow.dao.CalendarEventDao;
import com.example.timeflow.entity.CalendarEvent;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CalendarEventRepository {
    private CalendarEventDao calendarEventDao;
    private Executor executor;

    public CalendarEventRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.calendarEventDao = database.calendarEventDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface DataLoadListener {
        void onDataLoaded(List<CalendarEvent> events);
        void onError(Exception e);
    }

    public interface OperationListener {
        void onSuccess();
        void onError(Exception e);
    }

    public void loadAllEvents(DataLoadListener listener) {
        executor.execute(() -> {
            try {
                List<CalendarEvent> events = calendarEventDao.getAllEvents();
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onDataLoaded(events);
                    });
                }
            } catch (Exception e) {
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onError(e);
                    });
                }
            }
        });
    }

    public void loadEventsByDate(String date, DataLoadListener listener) {
        executor.execute(() -> {
            try {
                List<CalendarEvent> events = calendarEventDao.getEventsByDate(date);
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onDataLoaded(events);
                    });
                }
            } catch (Exception e) {
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onError(e);
                    });
                }
            }
        });
    }

    public void insertEvent(CalendarEvent event, OperationListener listener) {
        executor.execute(() -> {
            try {
                calendarEventDao.insertEvent(event);
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onSuccess();
                    });
                }
            } catch (Exception e) {
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onError(e);
                    });
                }
            }
        });
    }

    public void updateEvent(CalendarEvent event, OperationListener listener) {
        executor.execute(() -> {
            try {
                calendarEventDao.updateEvent(event);
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onSuccess();
                    });
                }
            } catch (Exception e) {
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onError(e);
                    });
                }
            }
        });
    }

    public void deleteEvent(CalendarEvent event, OperationListener listener) {
        executor.execute(() -> {
            try {
                calendarEventDao.deleteEvent(event);
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onSuccess();
                    });
                }
            } catch (Exception e) {
                if (listener != null) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                        listener.onError(e);
                    });
                }
            }
        });
    }
}