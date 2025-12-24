package com.example.timeflow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.timeflow.room.entity.User;
import com.example.timeflow.repository.UserRepository;

import java.util.concurrent.Executors;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        loadUser();
    }

    private void loadUser() {
        Executors.newSingleThreadExecutor().execute(() -> {
            userLiveData.postValue(repository.getLocalUser());
        });
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public void logout(Runnable callback) {
        repository.logout(callback);
    }
}
