package com.example.timeflow.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.timeflow.api.ApiClient;
import com.example.timeflow.api.AuthApi;
import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.User;

import java.util.concurrent.Executors;

public class UserRepository {

    private final UserDao userDao;
    private final AuthApi authApi;

    public UserRepository(Context context) {
        userDao = AppDatabase.getInstance(context).userDao();
        authApi = ApiClient.getInstance(context).create(AuthApi.class);
    }

    public LiveData<User> getLocalUserLive() {
        return userDao.getCurrentUser();
    }

    public void logout(Runnable onSuccess) {
        Executors.newSingleThreadExecutor().execute(() -> {
            userDao.clear();
            onSuccess.run();
        });
    }
}
