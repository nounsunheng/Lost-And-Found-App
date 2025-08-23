package com.kyle.lostandfoundapp.network;

import android.util.Log;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "http://10.0.2.2:8080/"; // For Android emulator
    // Use "http://192.168.1.xxx:8080/" for real device (replace with your computer's IP)

    private static Retrofit retrofit = null;
    private static ApiService apiService = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Create logging interceptor
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Create OkHttp client
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            apiService = getClient().create(ApiService.class);
        }
        return apiService;
    }

    public static String getImageUrl(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.w(TAG, "Image path is null or empty");
            return null;
        }

        String cleanPath = imagePath;

        // Handle different path formats
        if (imagePath.contains(":\\")) {
            // Windows absolute path like "D:/IT_Step/Android_Final/..."
            // Extract only the filename part after "uploads/"
            int uploadsIndex = imagePath.lastIndexOf("uploads/");
            if (uploadsIndex != -1) {
                cleanPath = "uploads/" + imagePath.substring(uploadsIndex + 8);
            } else {
                // Fallback: just get the filename
                int lastSlash = Math.max(imagePath.lastIndexOf('/'), imagePath.lastIndexOf('\\'));
                if (lastSlash != -1) {
                    cleanPath = "uploads/" + imagePath.substring(lastSlash + 1);
                }
            }
        } else {
            // Relative path - clean up and ensure proper format
            cleanPath = imagePath.replace("\\", "/");
            if (cleanPath.startsWith("/")) {
                cleanPath = cleanPath.substring(1);
            }
            // Ensure uploads/ prefix
            if (!cleanPath.startsWith("uploads/")) {
                cleanPath = "uploads/" + cleanPath;
            }
        }

        // Construct full URL
        String fullUrl = BASE_URL + cleanPath;

        Log.d(TAG, "Original path: " + imagePath);
        Log.d(TAG, "Clean path: " + cleanPath);
        Log.d(TAG, "Full URL: " + fullUrl);

        return fullUrl;
    }
}