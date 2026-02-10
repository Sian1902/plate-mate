package com.example.plate_mate.presentation.onboarding;

public class OnboardingItem {
    private int imageRes;
    private int title1;
    private int title2;
    private int description;

    public OnboardingItem(int imageRes, int title1, int title2, int description) {
        this.imageRes = imageRes;
        this.title1 = title1;
        this.title2 = title2;
        this.description = description;
    }

    public int getImageRes() {
        return imageRes;
    }

    public int getDescription() {
        return description;
    }

    public int getTitle1() {
        return title1;
    }

    public int getTitle2() {
        return title2;
    }
}
