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
    Completable preloadInitialData();

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


    Observable<List<PlannedMeal>> getPlannedMealsForNextSevenDays();

    Single<List<PlannedMeal>> getPlannedMealsByDate(Long date);


    Observable<List<PlannedMeal>> getAllPlannedMeals();

    Completable cleanupOldPlannedMeals();

    Completable deleteAllPlannedMeals();


}