package com.example.plate_mate.presentation.planner.view;

import com.example.plate_mate.data.meal.model.PlannedMeal;

import java.util.List;

public interface PlannerView {
    void showLoading();
    void hideLoading();
    void showPlannedMeals(List<PlannedMeal> plannedMeals);
    void showPlannedMealsForDate(Long date, List<PlannedMeal> meals);
    void showError(String message);
    void showSuccess(String message);

    void showEmptyState();

}
