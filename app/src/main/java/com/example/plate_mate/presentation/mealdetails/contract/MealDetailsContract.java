package com.example.plate_mate.presentation.mealdetails.contract;

import com.example.plate_mate.data.meal.model.Meal;

public interface MealDetailsContract {
    interface Presenter {
        void fetchMealDetails(String mealId);
        void detachView();
    }
    interface View {
        void showMealDetails(Meal meal);
        void showError(String message);
    }
}
