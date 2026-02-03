package com.example.plate_mate.data.meal.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class IngredientResponse {
    @SerializedName("meals")
    private List<Ingredient> meals;

    public List<Ingredient> getMeals() {
        return meals;
    }
}
