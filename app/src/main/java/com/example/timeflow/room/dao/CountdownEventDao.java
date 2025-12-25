package com.example.timeflow.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RoomWarnings;
import androidx.room.Update;

import com.example.timeflow.room.entity.CountdownEvent;
import java.util.List;

@Dao
public interface CountdownEventDao {
    @Insert
    void insert(CountdownEvent event);

    @Update
    void update(CountdownEvent event);

    @Query("DELETE FROM countdown_events WHERE id = :eventId")
    void deleteById(String eventId);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT COUNT(*) FROM countdown_events WHERE category_id = :categoryId")
    int getEventCountByCategoryId(int categoryId);

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT e.*, c.category_name as categoryName, c.color as categoryColor " +
            "FROM countdown_events e LEFT JOIN categories c ON e.category_id = c.id")
    List<CountdownEvent> getAllEventsWithCategory();

    @SuppressWarnings(RoomWarnings.CURSOR_MISMATCH)
    @Query("SELECT e.*, c.category_name as categoryName, c.color as categoryColor " +
            "FROM countdown_events e LEFT JOIN categories c ON e.category_id = c.id WHERE e.id = :eventId")
    CountdownEvent getEventById(String eventId);
}