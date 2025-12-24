package com.example.timeflow.datastore;

import android.content.Context;

import androidx.datastore.core.DataStore;
import androidx.datastore.preferences.core.PreferenceDataStoreFactory;
import androidx.datastore.preferences.core.Preferences;

import java.io.File;

public class DataStoreProvider {

    private static DataStore<Preferences> dataStore;

    public static synchronized DataStore<Preferences> get(Context context) {
        if (dataStore == null) {
            dataStore = PreferenceDataStoreFactory.INSTANCE.create(
                    () -> new File(context.getFilesDir(), "auth_prefs")
            );
        }
        return dataStore;
    }
}
