package com.example.plate_mate.presentation.splash.presenter;

import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.data.meal.repository.MealRepository;

import io.reactivex.rxjava3.core.Observable;

public class SplashPresenterImp implements SplashPresenter {

    private final MealRepository mealRepository;

    public SplashPresenterImp() {
        this.mealRepository = new MealRepoImp();
    }

    @Override
    public Observable<InitialMealData> preloadData() {
        return mealRepository.preloadInitialData();
    }
}