package com.example.plate_mate.util;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.repo.AuthRepo;
import com.example.plate_mate.data.auth.repo.AuthRepoImp;

/**
 * Utility class to manage dark mode across the application
 * Uses AuthRepo for proper layering
 */
public class DarkModeHelper {

    /**
     * Apply dark mode based on saved preference
     * Call this in Application.onCreate() or SplashActivity
     */
    public static void applyDarkMode(Context context) {
        AuthRepo authRepo = getAuthRepo(context);
        boolean isDarkModeEnabled = authRepo.isDarkModeEnabled();

        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Toggle dark mode and save preference
     */
    public static void toggleDarkMode(Context context, boolean isEnabled) {
        AuthRepo authRepo = getAuthRepo(context);

        // Save preference through repository
        authRepo.setDarkMode(isEnabled);

        // Apply immediately
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Check if dark mode is enabled
     */
    public static boolean isDarkModeEnabled(Context context) {
        AuthRepo authRepo = getAuthRepo(context);
        return authRepo.isDarkModeEnabled();
    }

    /**
     * Helper method to create AuthRepo instance
     */
    private static AuthRepo getAuthRepo(Context context) {
        AuthRemoteDataSource remoteDataSource = new AuthRemoteDataSource();
        AuthPrefManager prefManager = AuthPrefManager.getInstance(context);
        return new AuthRepoImp(remoteDataSource, prefManager);
    }
}