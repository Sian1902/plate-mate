package com.example.plate_mate.presentation.onboarding.presenter;

import com.example.plate_mate.presentation.onboarding.view.OnboardingItem;

import java.util.List;

public interface OnboardingPresenter {
    void setup();

    void onNextClicked();

    void onSkipClicked();

    void onPageSelected(int position);

    void onBackClicked();

    List<OnboardingItem> getOnboardingItems();
}
