package com.example.plate_mate.data.auth.datastore.local;

import android.content.Context;
import android.content.SharedPreferences;

public class AuthPrefManager {
    private static final String PREF_NAME = "PlateMate_Auth_Prefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_IS_GUEST = "isGuest";
    private static final String KEY_DARK_MODE = "dark_mode_enabled";

    private static AuthPrefManager instance;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    private AuthPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static synchronized AuthPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new AuthPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveUserSession(String userId, String email, boolean isGuest) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putBoolean(KEY_IS_GUEST, isGuest);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isGuest() {
        return sharedPreferences.getBoolean(KEY_IS_GUEST, false);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }
    public void clearSession() {
        editor.clear();
        editor.apply();
    }
    public void setDarkMode(boolean isEnabled) {
        editor.putBoolean(KEY_DARK_MODE, isEnabled); //
        editor.apply(); //
    }

    public boolean isDarkModeEnabled() {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false); //
    }
}