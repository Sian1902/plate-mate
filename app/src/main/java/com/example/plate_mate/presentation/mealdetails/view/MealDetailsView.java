package com.example.plate_mate.presentation.mealdetails.view;

import com.example.plate_mate.data.meal.model.Meal;

public interface MealDetailsView {
    void showMealDetails(Meal meal);

    void showError(String message);
}