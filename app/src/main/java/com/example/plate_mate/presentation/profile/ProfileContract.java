package com.example.plate_mate.presentation.profile;

import com.example.plate_mate.data.auth.model.User;

public interface ProfileContract {

    interface View {
        void showUserData(User user);
        void showLoading(boolean isLoading);
        void showError(String message);
        void showSuccess(String message);
        void navigateToLogin();
        void updateDarkModeSwitch(boolean isEnabled);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadUserProfile();
        void onDarkModeToggled(boolean isEnabled);
        void onResetPasswordClicked();
        void onLogoutClicked();
    }
}