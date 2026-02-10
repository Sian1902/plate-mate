package com.example.plate_mate.presentation.mealdetails.presenter;

import android.content.Context;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.presentation.mealdetails.contract.MealDetailsContract;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailsPresenter implements MealDetailsContract.Presenter{
    private final MealDetailsContract.View view;
    private final MealRepoImp repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public MealDetailsPresenter(MealDetailsContract.View view, Context context) {
        this.view = view;
        this.repository = MealRepoImp.getInstance(context);
    }

    public void fetchMealDetails(String mealId) {
        disposables.add(repository.getMealById(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                        view.showMealDetails(response.getMeals().get(0));
                    } else {
                        view.showError("Failed to load details");
                    }
                }, throwable -> {
                    view.showError("Network error");
                }));
    }

    public void detachView() {
        disposables.clear();
    }
}