package com.example.timeflow.room.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.timeflow.room.entity.User;

@Dao
public interface UserDao {

    @Query("SELECT * FROM User LIMIT 1")
    User getCurrentUser();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveUser(User user);

    @Query("DELETE FROM User")
    void clear();
}
