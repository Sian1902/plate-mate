package com.example.plate_mate.presentation.planner.presenter;

import com.example.plate_mate.data.meal.model.PlannedMeal;
import com.example.plate_mate.presentation.planner.view.PlannerView;

public interface PlannerPresenter {
    void attachView(PlannerView view);

    void detachView();

    void loadPlannedMealsForNextSevenDays();

    void loadPlannedMealsForDate(Long date);


    void cleanupOldMeals();

}