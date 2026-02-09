package com.example.plate_mate.presentation.saved.view;

import com.example.plate_mate.data.meal.model.Meal;

import java.util.List;
import java.util.Set;

public interface SavedView {
    void showFavorites(List<Meal> favorites);

    void updateFavorites(Set<String> favoriteMealIds);

    void showEmptyState();

    void hideEmptyState();

    void showError(String message);
}