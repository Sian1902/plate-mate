package com.example.plate_mate.presentation.planner.presenter;

import com.example.plate_mate.data.meal.model.PlannedMeal;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.example.plate_mate.presentation.planner.view.PlannerView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PlannerPresenterImp implements PlannerPresenter {

    private final MealRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private PlannerView view;

    public PlannerPresenterImp(MealRepository repository) {
        this.repository = repository;
    }

    @Override
    public void attachView(PlannerView view) {
        this.view = view;
    }

    @Override
    public void detachView() {
        this.view = null;
        disposables.clear();
    }

    @Override
    public void loadPlannedMealsForNextSevenDays() {
        if (view == null) return;

        view.showLoading();
        disposables.add(repository.getPlannedMealsForNextSevenDays().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(plannedMeals -> {
            if (view != null) {
                view.hideLoading();
                if (plannedMeals.isEmpty()) {
                    view.showEmptyState();
                } else {
                    view.showPlannedMeals(plannedMeals);
                }
            }
        }, error -> {
            if (view != null) {
                view.hideLoading();
                view.showError("Failed to load planned meals: " + error.getMessage());
            }
        }));
    }

    @Override
    public void loadPlannedMealsForDate(Long date) {
        if (view == null) return;

        view.showLoading();
        disposables.add(repository.getPlannedMealsByDate(date).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(meals -> {
            if (view != null) {
                view.hideLoading();
                view.showPlannedMealsForDate(date, meals);
            }
        }, error -> {
            if (view != null) {
                view.hideLoading();
                view.showError("Failed to load meals for date: " + error.getMessage());
            }
        }));
    }


    @Override
    public void cleanupOldMeals() {
        disposables.add(repository.cleanupOldPlannedMeals().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(() -> {
            if (view != null) {
                view.showSuccess("Old meals cleaned up");
                loadPlannedMealsForNextSevenDays();
            }
        }, error -> {
            if (view != null) {
                view.showError("Failed to cleanup old meals: " + error.getMessage());
            }
        }));
    }

}