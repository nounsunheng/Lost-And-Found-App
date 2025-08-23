package com.kyle.lostandfoundapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {

    private static final String PREF_NAME = "LostFoundPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_LANGUAGE = "language";

    // Theme modes
    public static final int THEME_MODE_SYSTEM = -1;
    public static final int THEME_MODE_LIGHT = 1;
    public static final int THEME_MODE_DARK = 2;

    // Language codes
    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_KHMER = "km";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private static SharedPreferencesManager instance;

    private SharedPreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    // User authentication methods
    public void saveUserData(String token, int userId, String username, String email, String phone, String role) {
        editor.putString(KEY_TOKEN, token);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_PHONE, phone);
        editor.putString(KEY_ROLE, role);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public void clearUserData() {
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_PHONE);
        editor.remove(KEY_ROLE);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getAuthHeader() {
        String token = getToken();
        return token != null ? "Bearer " + token : null;
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }

    public String getPhone() {
        return prefs.getString(KEY_PHONE, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "USER");
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getRole());
    }

    // Theme management methods
    public void setThemeMode(int themeMode) {
        editor.putInt(KEY_THEME_MODE, themeMode);
        editor.apply();
    }

    public int getThemeMode() {
        return prefs.getInt(KEY_THEME_MODE, THEME_MODE_SYSTEM);
    }

    public boolean isDarkTheme() {
        return getThemeMode() == THEME_MODE_DARK;
    }

    public boolean isLightTheme() {
        return getThemeMode() == THEME_MODE_LIGHT;
    }

    public boolean isSystemTheme() {
        return getThemeMode() == THEME_MODE_SYSTEM;
    }

    // Language management methods
    public void setLanguage(String languageCode) {
        editor.putString(KEY_LANGUAGE, languageCode);
        editor.apply();
    }

    public String getLanguage() {
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_ENGLISH);
    }

    public boolean isEnglishLanguage() {
        return LANGUAGE_ENGLISH.equals(getLanguage());
    }

    public boolean isKhmerLanguage() {
        return LANGUAGE_KHMER.equals(getLanguage());
    }

    // Utility methods for preferences
    public void saveBooleanPreference(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBooleanPreference(String key, boolean defaultValue) {
        return prefs.getBoolean(key, defaultValue);
    }

    public void saveStringPreference(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public String getStringPreference(String key, String defaultValue) {
        return prefs.getString(key, defaultValue);
    }

    public void saveIntPreference(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public int getIntPreference(String key, int defaultValue) {
        return prefs.getInt(key, defaultValue);
    }

    // Clear all preferences (for app reset)
    public void clearAllPreferences() {
        editor.clear();
        editor.apply();
    }

    // Get all stored preferences for debugging
    public void logAllPreferences() {
        android.util.Log.d("SharedPrefs", "=== All Preferences ===");
        android.util.Log.d("SharedPrefs", "IsLoggedIn: " + isLoggedIn());
        android.util.Log.d("SharedPrefs", "Username: " + getUsername());
        android.util.Log.d("SharedPrefs", "Email: " + getEmail());
        android.util.Log.d("SharedPrefs", "Role: " + getRole());
        android.util.Log.d("SharedPrefs", "Theme: " + getThemeMode());
        android.util.Log.d("SharedPrefs", "Language: " + getLanguage());
        android.util.Log.d("SharedPrefs", "Token present: " + (getToken() != null));
    }
}