package com.example.plate_mate.presentation.planner.presenter;

import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.example.plate_mate.presentation.planner.contract.PlannerContract;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PlannerPresenter implements PlannerContract.Presenter {

    private PlannerContract.View view;
    private final MealRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public PlannerPresenter(MealRepository repository) {
        this.repository = repository;
    }

    @Override
    public void attachView(PlannerContract.View view) {
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
        disposables.add(
                repository.getPlannedMealsForNextSevenDays()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                plannedMeals -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (plannedMeals.isEmpty()) {
                                            view.showEmptyState();
                                        } else {
                                            view.showPlannedMeals(plannedMeals);
                                        }
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Failed to load planned meals: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void loadPlannedMealsForDate(Long date) {
        if (view == null) return;

        view.showLoading();
        disposables.add(
                repository.getPlannedMealsByDate(date)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                meals -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showPlannedMealsForDate(date, meals);
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Failed to load meals for date: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void addPlannedMeal(Long date, MealType mealType, String mealId) {
        if (view == null) return;

        view.showLoading();
        disposables.add(
                repository.getMealById(mealId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                mealResponse -> {
                                    if (mealResponse.getMeals() != null && !mealResponse.getMeals().isEmpty()) {
                                        PlannedMeal plannedMeal = new PlannedMeal(
                                                date,
                                                mealType,
                                                mealId,
                                                mealResponse.getMeals().get(0),
                                                System.currentTimeMillis()
                                        );

                                        disposables.add(
                                                repository.insertPlannedMeal(plannedMeal)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(
                                                                () -> {
                                                                    if (view != null) {
                                                                        view.hideLoading();
                                                                        view.showMealAddedSuccess();
                                                                        loadPlannedMealsForNextSevenDays();
                                                                    }
                                                                },
                                                                error -> {
                                                                    if (view != null) {
                                                                        view.hideLoading();
                                                                        if (error.getMessage() != null &&
                                                                                error.getMessage().contains("within the next 7 days")) {
                                                                            view.showDateOutOfRangeError();
                                                                        } else {
                                                                            view.showError("Failed to add meal: " + error.getMessage());
                                                                        }
                                                                    }
                                                                }
                                                        )
                                        );
                                    } else {
                                        if (view != null) {
                                            view.hideLoading();
                                            view.showError("Meal not found");
                                        }
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Failed to fetch meal details: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void updatePlannedMeal(PlannedMeal plannedMeal) {
        if (view == null) return;

        view.showLoading();
        disposables.add(
                repository.updatePlannedMeal(plannedMeal)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showMealUpdatedSuccess();
                                        loadPlannedMealsForNextSevenDays();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        if (error.getMessage() != null &&
                                                error.getMessage().contains("within the next 7 days")) {
                                            view.showDateOutOfRangeError();
                                        } else {
                                            view.showError("Failed to update meal: " + error.getMessage());
                                        }
                                    }
                                }
                        )
        );
    }

    @Override
    public void removePlannedMeal(Long date, MealType mealType) {
        if (view == null) return;

        view.showLoading();
        disposables.add(
                repository.deletePlannedMealByDateAndType(date, mealType)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showMealRemovedSuccess();
                                        loadPlannedMealsForNextSevenDays();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Failed to remove meal: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void checkIfMealExists(Long date, MealType mealType) {
        if (view == null) return;

        disposables.add(
                repository.isPlannedMealExists(date, mealType)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                exists -> {
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError("Failed to check meal existence: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void cleanupOldMeals() {
        disposables.add(
                repository.cleanupOldPlannedMeals()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.showSuccess("Old meals cleaned up");
                                        loadPlannedMealsForNextSevenDays();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError("Failed to cleanup old meals: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void clearAllPlannedMeals() {
        if (view == null) return;

        view.showLoading();
        disposables.add(
                repository.deleteAllPlannedMeals()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showSuccess("All planned meals cleared");
                                        view.showEmptyState();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.hideLoading();
                                        view.showError("Failed to clear meals: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void getPlannedMealsCount() {
        disposables.add(
                repository.getPlannedMealsCount()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                count -> {
                                    if (view != null) {
                                        view.updateMealCount(count);
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showError("Failed to get meal count: " + error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void onMealClicked(PlannedMeal plannedMeal) {
        if (view != null) {
            view.navigateToMealDetails(plannedMeal);
        }
    }
}