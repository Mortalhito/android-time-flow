package com.example.timeflow.ui.register;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.timeflow.R;
import com.example.timeflow.ui.register.step.Step1EmailFragment;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.registerContainer, new Step1EmailFragment())
                    .commit();
        }
    }
}
