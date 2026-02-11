package com.example.plate_mate.presentation.mealdetails.presenter;

import android.content.Context;

import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.presentation.mealdetails.view.MealDetailsView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailsPresenter {
    private final MealDetailsView view;
    private final MealRepoImp repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public MealDetailsPresenter(MealDetailsView view, Context context) {
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

    public void checkIfFavorite(String mealId) {
        disposables.add(repository.getAllFavorites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favorites -> {
                    boolean isFavorite = false;
                    for (Meal meal : favorites) {
                        if (meal.getIdMeal().equals(mealId)) {
                            isFavorite = true;
                            break;
                        }
                    }
                    view.updateFavoriteStatus(isFavorite);
                }, throwable -> {
                    view.updateFavoriteStatus(false);
                }));
    }

    public void addToFavorites(Meal meal) {
        disposables.add(repository.insertFavorite(meal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.updateFavoriteStatus(true);
                    view.showFavoriteAdded();
                }, throwable -> {
                    view.showError("Failed to add to favorites");
                }));
    }

    public void removeFromFavorites(String mealId) {
        disposables.add(repository.deleteFavorite(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.updateFavoriteStatus(false);
                    view.showFavoriteRemoved();
                }, throwable -> {
                    view.showError("Failed to remove from favorites");
                }));
    }

    public void detachView() {
        disposables.clear();
    }
}