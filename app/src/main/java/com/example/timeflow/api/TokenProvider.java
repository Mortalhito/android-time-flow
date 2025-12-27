package com.example.timeflow.api;

import android.content.Context;

import com.example.timeflow.room.dao.UserDao;
import com.example.timeflow.room.database.AppDatabase;
import com.example.timeflow.room.entity.User;

public class TokenProvider {

    private static volatile String cachedToken;
    private static volatile User cachedUser; // 新增：缓存完整用户信息

    public static String getToken(Context context) {
        if (cachedToken != null) {
            return cachedToken;
        }

        User user = getUser(context);
        if (user != null) {
            cachedToken = user.getToken();
            return cachedToken;
        }
        return null;
    }

    // 新增：获取完整用户信息的方法
    public static User getUser(Context context) {
        if (cachedUser != null) {
            return cachedUser;
        }

        UserDao userDao = AppDatabase.getInstance(context).userDao();
        User user = userDao.getCurrentUser().getValue();
        if (user != null) {
            cachedUser = user;
            return user;
        }
        return null;
    }

    public static void clear() {
        cachedToken = null;
        cachedUser = null; // 新增：清除用户缓存
    }
}