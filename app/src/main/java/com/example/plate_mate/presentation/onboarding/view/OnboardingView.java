package com.example.plate_mate.presentation.onboarding.view;

public interface OnboardingView {
    void updateButtonText(int text);

    void navigateToAuth();

    int getCurrentPosition();

    void setCurrentPosition(int position);

    void goToNextPage();

    void goToPreviousPage();

    void showBackButton();

    void hideSkip();

    void hideBackButton();

    void showSkip();
}
