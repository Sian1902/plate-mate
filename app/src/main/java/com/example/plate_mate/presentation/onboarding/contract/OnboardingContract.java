package com.example.plate_mate.presentation.onboarding.contract;

import com.example.plate_mate.presentation.onboarding.OnboardingItem;

import java.util.List;

public interface OnboardingContract {
    interface Presenter {
        void setup();
        void onNextClicked();
        void onSkipClicked();
        void onPageSelected(int position);
        void onBackClicked();
        List<OnboardingItem> getOnboardingItems();
    }

    interface View {
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
}
