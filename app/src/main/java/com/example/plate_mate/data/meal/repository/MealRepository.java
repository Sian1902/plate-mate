package com.example.plate_mate.data.meal.repository;

import com.example.plate_mate.data.meal.model.InitialMealData;

import io.reactivex.rxjava3.core.Observable;

public interface MealRepository {
    Observable<InitialMealData> preloadInitialData();

    InitialMealData getCachedSplashData();
}
