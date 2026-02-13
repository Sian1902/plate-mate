package com.example.plate_mate.presentation.splash.presenter;

import android.content.Context;

import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.data.meal.repository.MealRepository;

import io.reactivex.rxjava3.core.Completable;

public class SplashPresenterImp implements SplashPresenter {

    private final MealRepository mealRepository;

    public SplashPresenterImp(Context context) {
        this.mealRepository = MealRepoImp.getInstance(context);
    }

    @Override
    public Completable preloadData() {
        return mealRepository.preloadInitialData();
    }
}