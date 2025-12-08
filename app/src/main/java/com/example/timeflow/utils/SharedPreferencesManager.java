package com.example.timeflow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.timeflow.entity.CountdownEvent;
import com.example.timeflow.entity.Habit;
import com.example.timeflow.entity.CalendarEvent;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesManager {
    private static final String PREFS_NAME = "TimeFlowPrefs";
    private SharedPreferences prefs;

    public SharedPreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // 使用简单的键值对存储，不需要Gson
    public void saveCountdownEvents(List<CountdownEvent> events) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("countdown_events_size", events.size());

        for (int i = 0; i < events.size(); i++) {
            CountdownEvent event = events.get(i);
            String prefix = "countdown_event_" + i + "_";
            editor.putString(prefix + "id", event.getId());
            editor.putString(prefix + "name", event.getName());
            editor.putString(prefix + "category", event.getCategory());
            editor.putString(prefix + "targetDate", event.getTargetDate());
        }
        editor.apply();
    }

    public List<CountdownEvent> getCountdownEvents() {
        List<CountdownEvent> events = new ArrayList<>();
        int size = prefs.getInt("countdown_events_size", 0);

        for (int i = 0; i < size; i++) {
            String prefix = "countdown_event_" + i + "_";
            String id = prefs.getString(prefix + "id", "");
            String name = prefs.getString(prefix + "name", "");
            String category = prefs.getString(prefix + "category", "");
            String targetDate = prefs.getString(prefix + "targetDate", "");

            if (!name.isEmpty()) {
                CountdownEvent event = new CountdownEvent(name, category, targetDate, 0);
                event.setId(id);
                events.add(event);
            }
        }
        return events;
    }

    // Habit存储方法
    public void saveHabits(List<Habit> habits) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("habits_size", habits.size());

        for (int i = 0; i < habits.size(); i++) {
            Habit habit = habits.get(i);
            String prefix = "habit_" + i + "_";
            editor.putString(prefix + "id", habit.getId());
            editor.putString(prefix + "name", habit.getName());
            editor.putInt(prefix + "totalDays", habit.getTotalDays());
            editor.putInt(prefix + "completedDays", habit.getCompletedDays());
            editor.putBoolean(prefix + "completedToday", habit.isCompletedToday());
        }
        editor.apply();
    }

    public List<Habit> getHabits() {
        List<Habit> habits = new ArrayList<>();
        int size = prefs.getInt("habits_size", 0);

        for (int i = 0; i < size; i++) {
            String prefix = "habit_" + i + "_";
            String id = prefs.getString(prefix + "id", "");
            String name = prefs.getString(prefix + "name", "");
            int totalDays = prefs.getInt(prefix + "totalDays", 0);
            int completedDays = prefs.getInt(prefix + "completedDays", 0);
            boolean completedToday = prefs.getBoolean(prefix + "completedToday", false);

            if (!name.isEmpty()) {
                Habit habit = new Habit(name, totalDays, completedDays);
                habit.setId(id);
                habit.setCompletedToday(completedToday);
                habits.add(habit);
            }
        }
        return habits;
    }

    // Calendar Events存储方法
    public void saveCalendarEvents(List<CalendarEvent> events) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("calendar_events_size", events.size());

        for (int i = 0; i < events.size(); i++) {
            CalendarEvent event = events.get(i);
            String prefix = "calendar_event_" + i + "_";
            editor.putString(prefix + "id", event.getId());
            editor.putString(prefix + "title", event.getTitle());
            editor.putString(prefix + "date", event.getDate());
            editor.putString(prefix + "priority", event.getPriority());
            editor.putString(prefix + "time", event.getTime());
            editor.putString(prefix + "description", event.getDescription() != null ? event.getDescription() : "");
            editor.putBoolean(prefix + "reminderEnabled", event.isReminderEnabled());
            editor.putString(prefix + "reminderTime", event.getReminderTime() != null ? event.getReminderTime() : "");
        }
        editor.apply();
    }

    public List<CalendarEvent> getCalendarEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        int size = prefs.getInt("calendar_events_size", 0);

        for (int i = 0; i < size; i++) {
            String prefix = "calendar_event_" + i + "_";
            String id = prefs.getString(prefix + "id", "");
            String title = prefs.getString(prefix + "title", "");
            String date = prefs.getString(prefix + "date", "");
            String priority = prefs.getString(prefix + "priority", "medium");
            String time = prefs.getString(prefix + "time", "");
            String description = prefs.getString(prefix + "description", "");
            boolean reminderEnabled = prefs.getBoolean(prefix + "reminderEnabled", false);
            String reminderTime = prefs.getString(prefix + "reminderTime", time);

            if (!title.isEmpty()) {
                CalendarEvent event = new CalendarEvent(title, date, priority, time);
                event.setId(id);
                event.setDescription(description);
                event.setReminderEnabled(reminderEnabled);
                event.setReminderTime(reminderTime);
                events.add(event);
            }
        }
        return events;
    }

    // 用户信息存储方法
    public void saveUserName(String name) {
        prefs.edit().putString("user_name", name).apply();
    }

    public String getUserName() {
        return prefs.getString("user_name", "用户");
    }

    public void saveUserEmail(String email) {
        prefs.edit().putString("user_email", email).apply();
    }

    public String getUserEmail() {
        return prefs.getString("user_email", "user@example.com");
    }

    public void saveTotalFocusTime(long minutes) {
        prefs.edit().putLong("total_focus_time", minutes).apply();
    }

    public long getTotalFocusTime() {
        return prefs.getLong("total_focus_time", 0);
    }

    public void addFocusTime(long minutes) {
        long current = getTotalFocusTime();
        saveTotalFocusTime(current + minutes);
    }

    // 添加、删除等方法
    public void addCountdownEvent(CountdownEvent event) {
        List<CountdownEvent> events = getCountdownEvents();
        events.add(event);
        saveCountdownEvents(events);
    }

    public void removeCountdownEvent(String eventId) {
        List<CountdownEvent> events = getCountdownEvents();
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(eventId)) {
                events.remove(i);
                break;
            }
        }
        saveCountdownEvents(events);
    }

    public void addHabit(Habit habit) {
        List<Habit> habits = getHabits();
        habits.add(habit);
        saveHabits(habits);
    }

    public void updateHabit(Habit habit) {
        List<Habit> habits = getHabits();
        for (int i = 0; i < habits.size(); i++) {
            if (habits.get(i).getId().equals(habit.getId())) {
                habits.set(i, habit);
                break;
            }
        }
        saveHabits(habits);
    }

    public void addCalendarEvent(CalendarEvent event) {
        List<CalendarEvent> events = getCalendarEvents();
        events.add(event);
        saveCalendarEvents(events);
    }

    public void removeCalendarEvent(String eventId) {
        List<CalendarEvent> events = getCalendarEvents();
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).getId().equals(eventId)) {
                events.remove(i);
                break;
            }
        }
        saveCalendarEvents(events);
    }
}