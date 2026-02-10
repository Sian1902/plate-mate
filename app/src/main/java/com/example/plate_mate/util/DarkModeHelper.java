package com.example.plate_mate.util;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.repo.AuthRepo;
import com.example.plate_mate.data.auth.repo.AuthRepoImp;

public class DarkModeHelper {

    public static void applyDarkMode(Context context) {
        AuthRepo authRepo = getAuthRepo(context);
        boolean isDarkModeEnabled = authRepo.isDarkModeEnabled();

        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
    public static void toggleDarkMode(Context context, boolean isEnabled) {
        AuthRepo authRepo = getAuthRepo(context);

        authRepo.setDarkMode(isEnabled);
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static boolean isDarkModeEnabled(Context context) {
        AuthRepo authRepo = getAuthRepo(context);
        return authRepo.isDarkModeEnabled();
    }
    private static AuthRepo getAuthRepo(Context context) {
        AuthRemoteDataSource remoteDataSource = new AuthRemoteDataSource();
        AuthPrefManager prefManager = AuthPrefManager.getInstance(context);
        return new AuthRepoImp(remoteDataSource, prefManager);
    }
}