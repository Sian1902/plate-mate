package com.example.plate_mate.presentation.profile.view;

import com.example.plate_mate.data.auth.model.User;

public interface ProfileView {
    void showUserData(User user);

    void showLoading(boolean isLoading);

    void showError(String message);

    void showSuccess(String message);

    void navigateToLogin();

    void updateDarkModeSwitch(boolean isEnabled);

    void showUploadComplete(int favoritesCount, int plannedMealsCount);
}
