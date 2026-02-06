package com.example.plate_mate.presentation.home.presenter;

public interface HomePresenter {
    void loadHomeData();

    void filterMeals(String category, String area, String ingredient);
}