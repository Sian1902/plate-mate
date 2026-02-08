package com.example.plate_mate.data.meal.repository;

import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.data.meal.model.MealResponse;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface MealRepository {
    Observable<InitialMealData> preloadInitialData();

    Single<InitialMealData> getCachedSplashData();

    // Filter methods
    Single<MealResponse> searchMealsByCategory(String category);

    Single<MealResponse> searchMealsByArea(String area);

    Single<MealResponse> searchMealsByIngredient(String ingredient);

    Observable<MealResponse> SearchMealsByName(String name);
}