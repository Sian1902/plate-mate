package com.example.plate_mate.data.meal.model;

public class InitialMealData {
    private CategorieListResponse categories;
    private IngredientResponse ingredients;
    private AreaResponse areas;
    private MealResponse meals;
    private MealResponse randomMeal;


    public InitialMealData(CategorieListResponse categories,
                           IngredientResponse ingredients,
                           AreaResponse areas,
                           MealResponse meals,
                           MealResponse randomMeal) {
        this.categories = categories;
        this.ingredients = ingredients;
        this.areas = areas;
        this.meals = meals;
        this.randomMeal = randomMeal;
    }

    public InitialMealData() {
    }

    public MealResponse getRandomMeal() {
        return randomMeal;
    }

    public CategorieListResponse getCategories() {
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