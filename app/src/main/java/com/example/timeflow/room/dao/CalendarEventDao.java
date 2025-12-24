package com.example.timeflow.room.dao;

import androidx.room.*;
import com.example.timeflow.room.entity.CalendarEvent;

import java.util.List;

@Dao
public interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events ORDER BY date, time")
    List<CalendarEvent> getAllEvents();

    @Query("SELECT * FROM calendar_events WHERE date = :date ORDER BY time")
    List<CalendarEvent> getEventsByDate(String date);

    @Query("SELECT * FROM calendar_events WHERE id = :id")
    CalendarEvent getEventById(String id);

    @Insert
    void insertEvent(CalendarEvent event);

    @Update
    void updateEvent(CalendarEvent event);

    @Delete
    void deleteEvent(CalendarEvent event);

    @Query("DELETE FROM calendar_events WHERE id = :id")
    void deleteEventById(String id);

    @Query("SELECT COUNT(*) FROM calendar_events WHERE date = :date")
    int getEventCountByDate(String date);
}