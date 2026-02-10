package com.example.plate_mate.presentation.home.contract;

import com.example.plate_mate.data.meal.model.Area;
import com.example.plate_mate.data.meal.model.Category;
import com.example.plate_mate.data.meal.model.Ingredient;
import com.example.plate_mate.data.meal.model.Meal;

import java.util.List;
import java.util.Set;

public interface HomeContract {
    interface Presenter {
        void loadHomeData();
        void filterMeals(String category, String area, String ingredient);
        void searchMeals(String query);
        void clearAllFilters();
        void toggleFavorite(Meal meal);
        void loadFavorites();
    }
    interface View {
        void setupUi(List<Meal> mealList, Meal heroMeal);

        void setFilterOptions(List<Category> categories, List<Area> areas, List<Ingredient> ingredients);

        void updateMealList(List<Meal> meals);

        void updateFavorites(Set<String> favoriteMealIds);

        void showError(String message);
    }
}
