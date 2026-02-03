package com.example.plate_mate.data.meal.datasource.remote;

import com.example.plate_mate.data.meal.model.AreaResponse;
import com.example.plate_mate.data.meal.model.CategoryResponse;
import com.example.plate_mate.data.meal.model.IngredientResponse;
import com.example.plate_mate.data.meal.model.MealResponse;
import com.example.plate_mate.data.meal.network.RetrofitClient;

import io.reactivex.rxjava3.core.Observable;

public class MealRemoteDataSource {
    private final MealService mealService;

    public MealRemoteDataSource() {
        this.mealService = RetrofitClient.getMealApiService();
    }

    public Observable<MealResponse> searchMealByName(String name) {
        return mealService.searchMealByName(name);
    }

    public Observable<MealResponse> getMealById(String id) {
        return mealService.getMealById(id);
    }

    public Observable<MealResponse> getRandomMeal() {
        return mealService.getRandomMeal();
    }

    public Observable<CategoryResponse> getCategories() {
        return mealService.getCategories();
    }

    public Observable<MealResponse> filterByIngredient(String ingredient) {
        return mealService.filterByIngredient(ingredient);
    }

    public Observable<MealResponse> filterByCategory(String category) {
        return mealService.filterByCategory(category);
    }

    public Observable<MealResponse> filterByArea(String area) {
        return mealService.filterByArea(area);
    }

    public Observable<CategoryResponse> listCategories() {
        return mealService.listCategories();
    }

    public Observable<AreaResponse> listAreas() {
        return mealService.listAreas();
    }

    public Observable<IngredientResponse> listIngredients() {
        return mealService.listIngredients();
    }

    public Observable<MealResponse> searchMealByFirstLetter(String letter) {
        return mealService.searchMealByFirstLetter(letter);
    }
}