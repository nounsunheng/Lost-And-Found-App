package com.kyle.lostandfoundapp.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.ComponentActivity;
import androidx.core.splashscreen.SplashScreen; // Android 12+ splash API

import com.kyle.lostandfoundapp.MainActivity;
import com.kyle.lostandfoundapp.R;

public class SplashActivity extends ComponentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install the splash screen
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // Optional: delay to show splash (e.g., 2 seconds)
        getWindow().getDecorView().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
        }, 2000); // 2000 ms = 2 seconds
    }
}
