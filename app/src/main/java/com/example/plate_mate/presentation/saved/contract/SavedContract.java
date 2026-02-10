package com.example.plate_mate.presentation.saved.contract;

import com.example.plate_mate.data.meal.model.Meal;

import java.util.List;
import java.util.Set;

public interface SavedContract {
    interface Presenter {
        void loadFavorites();

        void toggleFavorite(Meal meal);

        void dispose();
    }
    interface View {
        void showFavorites(List<Meal> favorites);

        void updateFavorites(Set<String> favoriteMealIds);

        void showEmptyState();

        void hideEmptyState();

        void showError(String message);
    }
}
