package com.example.plate_mate.presentation.splash.presenter;

import android.content.Context;

import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.data.meal.repository.MealRepository;

import io.reactivex.rxjava3.core.Observable;

public class SplashPresenterImp implements SplashPresenter {

    private final MealRepository mealRepository;

    public SplashPresenterImp(Context context) {
        this.mealRepository = MealRepoImp.getInstance(context);
    }

    @Override
    public Observable<InitialMealData> preloadData() {
        return mealRepository.preloadInitialData();
    }
}