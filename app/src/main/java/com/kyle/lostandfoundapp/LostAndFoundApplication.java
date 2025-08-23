package com.kyle.lostandfoundapp;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.util.Log;

import androidx.appcompat.app.AppCompatDelegate;

import com.kyle.lostandfoundapp.utils.LocaleManager;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

public class LostAndFoundApplication extends Application {

    private static final String TAG = "LostAndFoundApp";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "=== Application onCreate ===");

        // Initialize SharedPreferences
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(this);
        Log.d(TAG, "SharedPreferencesManager initialized");

        // Set app theme based on saved preference
        int themeMode = prefsManager.getThemeMode();
        int appCompatMode = mapToAppCompatThemeMode(themeMode);
        AppCompatDelegate.setDefaultNightMode(appCompatMode);
        Log.d(TAG, "Theme applied - Custom mode: " + themeMode + ", AppCompat mode: " + appCompatMode);

        // Log current settings
        Log.d(TAG, "Current language: " + prefsManager.getLanguage());
        Log.d(TAG, "Current theme: " + themeMode);
        Log.d(TAG, "Is logged in: " + prefsManager.isLoggedIn());

        Log.d(TAG, "=== Application initialization completed ===");
    }

    @Override
    protected void attachBaseContext(Context base) {
        Log.d(TAG, "=== Application attachBaseContext ===");

        // Apply saved language on app start
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(base);
        String language = prefsManager.getLanguage();

        Log.d(TAG, "Applying language to application context: " + language);

        Context context = LocaleManager.setLocale(base, language);
        super.attachBaseContext(context);

        Log.d(TAG, "Base context attached with language: " + language);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "=== Configuration changed ===");

        // Reapply language when configuration changes (like system language change)
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(this);
        String language = prefsManager.getLanguage();

        Log.d(TAG, "Configuration changed, reapplying language: " + language);
        LocaleManager.setLocale(this, language);

        Log.d(TAG, "Configuration change handled");
    }

    /**
     * Map custom theme modes to AppCompat theme modes
     * @param customThemeMode Custom theme mode from SharedPreferencesManager
     * @return AppCompat theme mode
     */
    private int mapToAppCompatThemeMode(int customThemeMode) {
        switch (customThemeMode) {
            case SharedPreferencesManager.THEME_MODE_LIGHT:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case SharedPreferencesManager.THEME_MODE_DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case SharedPreferencesManager.THEME_MODE_SYSTEM:
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}