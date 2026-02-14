package com.example.plate_mate.presentation.onboarding.presenter;

import com.example.plate_mate.R;
import com.example.plate_mate.presentation.onboarding.view.OnboardingItem;
import com.example.plate_mate.presentation.onboarding.view.OnboardingView;

import java.util.ArrayList;
import java.util.List;

public class OnboardingPresenterImp implements OnboardingPresenter {
    private final OnboardingView view;
    private int currentPosition = 0;
    private List<OnboardingItem> items;

    public OnboardingPresenterImp(OnboardingView view) {
        this.view = view;
    }

    @Override
    public void setup() {
        items = new ArrayList<>();
        items.add(new OnboardingItem(R.drawable.onboarding_page1, R.string.onboarding_page1_title1, R.string.onboarding_page_1_title2, R.string.onboarding_page_1_description));
        items.add(new OnboardingItem(R.drawable.onboarding_page2, R.string.onboarding_page2_title1, R.string.onboarding_page_2_title2, R.string.onboarding_page_2_description));

        view.setCurrentPosition(0);
        onPageSelected(0);
    }

    @Override
    public void onNextClicked() {
        if (currentPosition < items.size() - 1) {
            view.goToNextPage();
        } else {
            view.navigateToAuth();
        }
    }

    @Override
    public void onSkipClicked() {
        view.navigateToAuth();
    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        view.setCurrentPosition(position);

        if (position == items.size() - 1) {
            view.updateButtonText(R.string.get_started);
            view.hideSkip();
        } else {
            view.updateButtonText(R.string.next);
            view.showSkip();
        }

        if (position > 0) {
            view.showBackButton();
        } else {
            view.hideBackButton();
        }
    }

    @Override
    public void onBackClicked() {
        if (currentPosition > 0) {
            view.goToPreviousPage();
        }
    }

    @Override
    public List<OnboardingItem> getOnboardingItems() {
        return items;
    }
}
