package com.example.timeflow.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.timeflow.room.entity.User;

import java.util.List;

@Dao
public interface UserDao {

    @Query("SELECT * FROM User LIMIT 1")
    LiveData<User> getCurrentUser();

    @Query("SELECT * FROM User")  // 新增：同步获取所有（通常只有1个）
    List<User> getAllUsersBlocking();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void saveUser(User user);

    @Query("DELETE FROM User")
    void clear();
}
