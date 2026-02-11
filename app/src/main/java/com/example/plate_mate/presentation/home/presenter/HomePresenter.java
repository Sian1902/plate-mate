package com.example.plate_mate.presentation.home.presenter;

import com.example.plate_mate.data.meal.model.Meal;

public interface HomePresenter {
    void loadHomeData();

    void filterMeals(String category, String area, String ingredient);

    void searchMeals(String query);

    void clearAllFilters();

    void toggleFavorite(Meal meal);

    void loadFavorites();
}