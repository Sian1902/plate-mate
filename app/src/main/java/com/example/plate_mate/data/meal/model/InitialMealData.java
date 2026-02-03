package com.example.plate_mate.data.meal.model;

public class InitialMealData {
    private CategoryResponse categories;
    private IngredientResponse ingredients;
    private AreaResponse areas;
    private MealResponse meals;

    public InitialMealData(CategoryResponse categories,
                           IngredientResponse ingredients,
                           AreaResponse areas,
                           MealResponse meals) {
        this.categories = categories;
        this.ingredients = ingredients;
        this.areas = areas;
        this.meals = meals;
    }

    public CategoryResponse getCategories() {
        return categories;
    }

    public IngredientResponse getIngredients() {
        return ingredients;
    }

    public AreaResponse getAreas() {
        return areas;
    }

    public MealResponse getMeals() {
        return meals;
    }
}