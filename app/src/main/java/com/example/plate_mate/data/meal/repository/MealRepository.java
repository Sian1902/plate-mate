package com.example.plate_mate.data.meal.repository;

import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.model.MealResponse;
import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public interface MealRepository {
    Observable<InitialMealData> preloadInitialData();

    Single<InitialMealData> getCachedSplashData();

    Single<MealResponse> searchMealsByCategory(String category);

    Single<MealResponse> searchMealsByArea(String area);

    Single<MealResponse> searchMealsByIngredient(String ingredient);

    Observable<MealResponse> SearchMealsByName(String name);

    Single<MealResponse> getMealById(String id);

    Completable insertFavorite(Meal favorite);

    Observable<List<Meal>> getAllFavorites();


    Completable deleteFavorite(String mealId);

    Completable insertPlannedMeal(PlannedMeal plannedMeal);

    Completable insertPlannedMeals(List<PlannedMeal> plannedMeals);

    Completable updatePlannedMeal(PlannedMeal plannedMeal);


    Completable deletePlannedMealByDateAndType(Long date, MealType mealType);

    Observable<List<PlannedMeal>> getPlannedMealsForNextSevenDays();

    Single<List<PlannedMeal>> getPlannedMealsByDate(Long date);

    Single<PlannedMeal> getPlannedMealByDateAndType(Long date, MealType mealType);

    Single<Boolean> isPlannedMealExists(Long date, MealType mealType);

    Observable<List<PlannedMeal>> getAllPlannedMeals();

    Completable cleanupOldPlannedMeals();

    Completable deleteAllPlannedMeals();

    Single<Integer> getPlannedMealsCount();

}