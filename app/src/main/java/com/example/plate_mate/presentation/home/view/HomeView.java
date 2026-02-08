package com.example.plate_mate.presentation.home.view;

import com.example.plate_mate.data.meal.model.Area;
import com.example.plate_mate.data.meal.model.Category;
import com.example.plate_mate.data.meal.model.Ingredient;
import com.example.plate_mate.data.meal.model.Meal;

import java.util.List;

public interface HomeView {
    void setupUi(List<Meal> mealList, Meal heroMeal);

    void setFilterOptions(List<Category> categories, List<Area> areas, List<Ingredient> ingredients);

    void updateMealList(List<Meal> meals);


    void showError(String message);
}