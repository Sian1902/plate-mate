package com.example.plate_mate.presentation.splash.presenter;

import com.example.plate_mate.data.meal.model.InitialMealData;

import io.reactivex.rxjava3.core.Observable;

public interface SplashPresenter {

    Observable<InitialMealData> preloadData();


}