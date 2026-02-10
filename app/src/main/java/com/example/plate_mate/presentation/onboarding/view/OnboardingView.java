package com.example.plate_mate.presentation.onboarding.view;

public interface OnboardingView {
    void updateButtonText(int text);
    void navigateToAuth();
    void setCurrentPosition(int position);
    int getCurrentPosition();
    void goToNextPage();
    void goToPreviousPage();
    void showBackButton();
    void hideSkip();
    void hideBackButton();
    void showSkip();
}
