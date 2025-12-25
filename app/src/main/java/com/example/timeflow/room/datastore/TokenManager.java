package com.example.timeflow.room.datastore;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {

    private static final String PREF_NAME = "auth";
    private static final String KEY_TOKEN = "token";

    private final SharedPreferences sp;

    public TokenManager(Context context) {
        sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveToken(String token) {
        sp.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return sp.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        sp.edit().remove(KEY_TOKEN).apply();
    }

    public String getTokenBlocking() {
        return sp.getString("token", null);
    }
}
