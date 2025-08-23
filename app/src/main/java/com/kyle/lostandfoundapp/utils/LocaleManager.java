package com.kyle.lostandfoundapp.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

public class LocaleManager {

    private static final String TAG = "LocaleManager";

    public static final String LANGUAGE_ENGLISH = "en";
    public static final String LANGUAGE_KHMER = "km";

    private Context context;

    public LocaleManager(Context context) {
        this.context = context;
    }

    /**
     * Set the app locale
     * @param context Context
     * @param language Language code (en, km)
     * @return Context with updated locale
     */
    public static Context setLocale(Context context, String language) {
        Log.d(TAG, "Setting locale to: " + language);
        return updateResources(context, language);
    }

    /**
     * Set new locale and restart activity
     * @param activity Current activity
     * @param language Language code
     */
    public void setNewLocale(Activity activity, String language) {
        Log.d(TAG, "Setting new locale and restarting activity: " + language);

        // Save language preference
        SharedPreferencesManager.getInstance(activity).setLanguage(language);

        // Update resources
        updateResources(activity, language);

        // Create new intent to restart the activity
        Intent intent = new Intent(activity, activity.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Transfer any existing extras
        if (activity.getIntent() != null && activity.getIntent().getExtras() != null) {
            intent.putExtras(activity.getIntent().getExtras());
        }

        activity.startActivity(intent);
        activity.finish();

        // Add transition animation
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    /**
     * Get current locale
     * @param context Context
     * @return Current locale string
     */
    public static String getLocale(Context context) {
        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(context);
        String language = prefsManager.getLanguage();
        Log.d(TAG, "Current locale: " + language);
        return language;
    }

    /**
     * Update context resources with new locale
     * @param context Context to update
     * @param language Language code
     * @return Updated context
     */
    private static Context updateResources(Context context, String language) {
        Log.d(TAG, "Updating resources for language: " + language);

        Locale locale = getLocaleFromLanguageCode(language);
        Locale.setDefault(locale);

        Configuration config = new Configuration(context.getResources().getConfiguration());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLocales(new android.os.LocaleList(locale));
            Context updatedContext = context.createConfigurationContext(config);
            Log.d(TAG, "Resources updated for API >= N");
            return updatedContext;
        } else {
            config.locale = locale;
            Resources resources = context.getResources();
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            resources.updateConfiguration(config, displayMetrics);
            Log.d(TAG, "Resources updated for API < N");
            return context;
        }
    }

    /**
     * Get Locale object from language code
     * @param languageCode Language code (en, km)
     * @return Locale object
     */
    private static Locale getLocaleFromLanguageCode(String languageCode) {
        Locale locale;
        switch (languageCode) {
            case LANGUAGE_KHMER:
                locale = new Locale(LANGUAGE_KHMER, "KH"); // Khmer (Cambodia)
                break;
            case LANGUAGE_ENGLISH:
            default:
                // Default to English for any unknown language codes or null values
                locale = new Locale(LANGUAGE_ENGLISH, "US"); // English (US)
                break;
        }
        Log.d(TAG, "Created locale: " + locale.toString());
        return locale;
    }

    /**
     * Check if current locale is RTL
     * @param context Context
     * @return true if RTL, false otherwise
     */
    public static boolean isRTL(Context context) {
        String language = getLocale(context);
        // Add RTL language codes here if needed
        // Khmer is LTR, so return false for now
        return false;
    }

    /**
     * Get language display name
     * @param languageCode Language code
     * @return Display name
     */
    public static String getLanguageDisplayName(String languageCode) {
        switch (languageCode) {
            case LANGUAGE_KHMER:
                return "ភាសាខ្មែរ";
            case LANGUAGE_ENGLISH:
            default:
                return "English";
        }
    }

    /**
     * Get available languages
     * @return Array of available language codes
     */
    public static String[] getAvailableLanguages() {
        return new String[]{LANGUAGE_ENGLISH, LANGUAGE_KHMER};
    }

    /**
     * Check if language is supported
     * @param languageCode Language code to check
     * @return true if supported, false otherwise
     */
    public static boolean isLanguageSupported(String languageCode) {
        for (String lang : getAvailableLanguages()) {
            if (lang.equals(languageCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Apply locale to activity context
     * @param activity Activity to apply locale to
     */
    public static void applyLocaleToActivity(Activity activity) {
        String language = getLocale(activity);
        Log.d(TAG, "Applying locale to activity: " + language);

        Locale locale = getLocaleFromLanguageCode(language);
        Locale.setDefault(locale);

        Configuration config = activity.getResources().getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            config.setLocales(new android.os.LocaleList(locale));
        } else {
            config.locale = locale;
        }

        activity.getResources().updateConfiguration(config, activity.getResources().getDisplayMetrics());
        Log.d(TAG, "Locale applied to activity successfully");
    }
}