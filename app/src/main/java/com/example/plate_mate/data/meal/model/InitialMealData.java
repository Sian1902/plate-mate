package com.example.plate_mate.data.meal.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InitialMealData {
    private CategoryListResponse categories;
    private IngredientResponse ingredients;
    private AreaResponse areas;
    private MealResponse meals;
    private MealResponse randomMeal;

}