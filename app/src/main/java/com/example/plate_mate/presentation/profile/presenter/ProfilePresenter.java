package com.example.plate_mate.presentation.profile.presenter;


import com.example.plate_mate.presentation.profile.view.ProfileView;

public interface ProfilePresenter {
    void attachView(ProfileView view);

    void detachView();

    void loadUserProfile();

    void onDarkModeToggled(boolean isEnabled);

    void onResetPasswordClicked();

    void onLogoutClicked();

    void onUploadDataClicked();
}
