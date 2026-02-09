package com.example.plate_mate.presentation.saved.presenter;

import com.example.plate_mate.data.meal.model.Meal;

public interface SavedPresenter {
    void loadFavorites();

    void toggleFavorite(Meal meal);

    void dispose();
}