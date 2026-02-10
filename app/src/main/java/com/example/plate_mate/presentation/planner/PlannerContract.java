package com.example.plate_mate.presentation.planner;

import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;

import java.util.List;

public interface PlannerContract {

    interface View {
        void showLoading();
        void hideLoading();
        void showPlannedMeals(List<PlannedMeal> plannedMeals);
        void showPlannedMealsForDate(Long date, List<PlannedMeal> meals);
        void showError(String message);
        void showSuccess(String message);
        void showMealAddedSuccess();
        void showMealRemovedSuccess();
        void showMealUpdatedSuccess();
        void showDateOutOfRangeError();
        void navigateToMealDetails(PlannedMeal plannedMeal);
        void showEmptyState();
        void updateMealCount(int count);
    }

    interface Presenter {
        void attachView(View view);
        void detachView();
        void loadPlannedMealsForNextSevenDays();
        void loadPlannedMealsForDate(Long date);
        void addPlannedMeal(Long date, MealType mealType, String mealId);
        void updatePlannedMeal(PlannedMeal plannedMeal);
        void removePlannedMeal(Long date, MealType mealType);
        void checkIfMealExists(Long date, MealType mealType);
        void cleanupOldMeals();
        void clearAllPlannedMeals();
        void getPlannedMealsCount();
        void onMealClicked(PlannedMeal plannedMeal);
    }
}