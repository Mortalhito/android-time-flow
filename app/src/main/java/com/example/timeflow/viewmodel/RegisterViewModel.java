package com.example.timeflow.viewmodel;


import android.net.Uri;

import androidx.lifecycle.ViewModel;

public class RegisterViewModel extends ViewModel {

    // step1
    public String email = "";
    public String code = "";

    // step2
    public String username = "";
    public String password = "";

    // step3
    public String nickname = "";
    public Uri avatarUri = null;
}
