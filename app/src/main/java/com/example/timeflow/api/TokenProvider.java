package com.example.timeflow.api;

import android.content.Context;

import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.User;

public class TokenProvider {

    private static volatile String cachedToken;

    public static String getToken(Context context) {
        if (cachedToken != null) {
            return cachedToken;
        }

        UserDao userDao = AppDatabase.getInstance(context).userDao();
        User user = userDao.getCurrentUser().getValue();
        if (user != null) {
            cachedToken = user.getToken();
            return cachedToken;
        }
        return null;
    }

    public static void clear() {
        cachedToken = null;
    }
}
