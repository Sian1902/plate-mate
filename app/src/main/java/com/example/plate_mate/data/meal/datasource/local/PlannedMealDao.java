package com.example.plate_mate.data.meal.datasource.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

@Dao
public interface PlannedMealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlannedMeal(PlannedMeal plannedMeal);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPlannedMeals(List<PlannedMeal> plannedMeals);

    @Update
    void updatePlannedMeal(PlannedMeal plannedMeal);

    @Delete
    void deletePlannedMeal(PlannedMeal plannedMeal);

    @Query("DELETE FROM planned_meals WHERE date = :date AND meal_type = :mealType")
    void deletePlannedMealByDateAndType(Long date, MealType mealType);

    @Query("SELECT * FROM planned_meals WHERE date >= :startDate AND date < :endDate ORDER BY date ASC, meal_type ASC")
    Observable<List<PlannedMeal>> getPlannedMealsInRange(Long startDate, Long endDate);

    @Query("SELECT * FROM planned_meals WHERE date = :date ORDER BY meal_type ASC")
    Single<List<PlannedMeal>> getPlannedMealsByDate(Long date);

    @Query("SELECT * FROM planned_meals WHERE date = :date AND meal_type = :mealType")
    Single<PlannedMeal> getPlannedMealByDateAndType(Long date, MealType mealType);

    @Query("SELECT EXISTS(SELECT 1 FROM planned_meals WHERE date = :date AND meal_type = :mealType)")
    Single<Boolean> isPlannedMealExists(Long date, MealType mealType);

    @Query("SELECT * FROM planned_meals ORDER BY date ASC, meal_type ASC")
    Observable<List<PlannedMeal>> getAllPlannedMeals();

    @Query("DELETE FROM planned_meals WHERE date < :date")
    void deleteOldPlannedMeals(Long date);
    @Query("DELETE FROM planned_meals")
    void deleteAllPlannedMeals();

    @Query("SELECT COUNT(*) FROM planned_meals WHERE date >= :startDate AND date < :endDate")
    Single<Integer> getPlannedMealsCount(Long startDate, Long endDate);

    @Query("SELECT * FROM planned_meals WHERE meal_type = :mealType AND date >= :startDate ORDER BY date ASC")
    Observable<List<PlannedMeal>> getPlannedMealsByType(MealType mealType, Long startDate);
}