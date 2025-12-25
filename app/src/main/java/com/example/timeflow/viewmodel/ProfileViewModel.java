package com.example.timeflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.timeflow.repository.UserRepository;
import com.example.timeflow.room.entity.User;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final LiveData<User> userLiveData;

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        userLiveData = repository.getLocalUserLive();
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public void logout(Runnable callback) {
        repository.logout(callback);
    }
}
