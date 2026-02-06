package com.example.plate_mate.presentation.home.presenter;

import android.content.Context;
import android.util.Log;

import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.example.plate_mate.presentation.home.view.HomeView;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomePresenterImp implements HomePresenter {
    private MealRepository mealRepo;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private HomeView homeView;

    public HomePresenterImp(Context context, HomeView homeView) {
        this.mealRepo = MealRepoImp.getInstance(context);
        this.homeView = homeView;
    }

    @Override
    public void loadHomeData() {
        homeView.showLoading();

        disposables.add(mealRepo.getCachedSplashData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    homeView.hideLoading();

                    if (data != null) {
                        if (data.getCategories() != null && data.getAreas() != null && data.getIngredients() != null) {
                            homeView.setFilterOptions(
                                    data.getCategories().getMeal(),
                                    data.getAreas().getMeals(),
                                    data.getIngredients().getMeals()
                            );
                        }

                        if (data.getMeals() != null && data.getRandomMeal() != null) {
                            List<Meal> meals = data.getMeals().getMeals();
                            homeView.setupUi(meals, data.getRandomMeal().getMeals().get(0));
                            Log.d("HomePresenterImp", "Meals loaded: " + meals.size());
                        }
                    }
                }, throwable -> {
                    homeView.hideLoading();
                    homeView.showError("Error loading data: " + throwable.getMessage());
                    Log.e("HomePresenterImp", "Error loading meals: " + throwable.getMessage());
                }));
    }

    @Override
    public void filterMeals(String category, String area, String ingredient) {
        homeView.showLoading();

        // Determine which filter to apply (prioritize category, then area, then ingredient)
        if (category != null) {
            filterByCategory(category);
        } else if (area != null) {
            filterByArea(area);
        } else if (ingredient != null) {
            filterByIngredient(ingredient);
        } else {
            // No filters - load default data
            loadHomeData();
        }
    }

    private void filterByCategory(String category) {
        disposables.add(mealRepo.searchMealsByCategory(category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mealResponse -> {
                    homeView.hideLoading();

                    if (mealResponse != null && mealResponse.getMeals() != null) {
                        homeView.updateMealList(mealResponse.getMeals());
                        Log.d("HomePresenterImp", "Filtered by category: " + category);
                    }
                }, throwable -> {
                    homeView.hideLoading();
                    homeView.showError("Error filtering by category");
                    Log.e("HomePresenterImp", "Error: " + throwable.getMessage());
                }));
    }

    private void filterByArea(String area) {
        disposables.add(mealRepo.searchMealsByArea(area)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mealResponse -> {
                    homeView.hideLoading();

                    if (mealResponse != null && mealResponse.getMeals() != null) {
                        homeView.updateMealList(mealResponse.getMeals());
                        Log.d("HomePresenterImp", "Filtered by area: " + area);
                    }
                }, throwable -> {
                    homeView.hideLoading();
                    homeView.showError("Error filtering by country");
                    Log.e("HomePresenterImp", "Error: " + throwable.getMessage());
                }));
    }

    private void filterByIngredient(String ingredient) {
        disposables.add(mealRepo.searchMealsByIngredient(ingredient)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mealResponse -> {
                    homeView.hideLoading();

                    if (mealResponse != null && mealResponse.getMeals() != null) {
                        homeView.updateMealList(mealResponse.getMeals());
                        Log.d("HomePresenterImp", "Filtered by ingredient: " + ingredient);
                    }
                }, throwable -> {
                    homeView.hideLoading();
                    homeView.showError("Error filtering by ingredient");
                    Log.e("HomePresenterImp", "Error: " + throwable.getMessage());
                }));
    }

    public void dispose() {
        disposables.clear();
    }
}