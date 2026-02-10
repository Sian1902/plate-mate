package com.example.plate_mate.data.meal.datasource.local.plannned;

import android.content.Context;

import com.example.plate_mate.data.database.MealsDatabase;
import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;

import java.util.Calendar;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class PlannedMealLocalDataStore {

    private static final int MAX_PLANNING_DAYS = 7;
    private final PlannedMealDao plannedMealDao;

    public PlannedMealLocalDataStore(Context context) {
        MealsDatabase database = MealsDatabase.getInstance(context);
        this.plannedMealDao = database.plannedMealDao();
    }

    public void insertPlannedMeal(PlannedMeal plannedMeal) {
        validateDateWithinSevenDays(plannedMeal.getDate());
        plannedMeal.setCreatedAt(System.currentTimeMillis());
        plannedMealDao.insertPlannedMeal(plannedMeal);
    }

    public void insertPlannedMeals(List<PlannedMeal> plannedMeals) {
        for (PlannedMeal meal : plannedMeals) {
            validateDateWithinSevenDays(meal.getDate());
            meal.setCreatedAt(System.currentTimeMillis());
        }
        plannedMealDao.insertPlannedMeals(plannedMeals);
    }

    public void updatePlannedMeal(PlannedMeal plannedMeal) {
        validateDateWithinSevenDays(plannedMeal.getDate());
        plannedMealDao.updatePlannedMeal(plannedMeal);
    }

    public void deletePlannedMeal(PlannedMeal plannedMeal) {
        plannedMealDao.deletePlannedMeal(plannedMeal);
    }

    public void deletePlannedMealByDateAndType(Long date, MealType mealType) {
        plannedMealDao.deletePlannedMealByDateAndType(date, mealType);
    }

    public Observable<List<PlannedMeal>> getPlannedMealsForNextSevenDays() {
        long[] dateRange = getSevenDayRange();
        return plannedMealDao.getPlannedMealsInRange(dateRange[0], dateRange[1]);
    }


    public Single<List<PlannedMeal>> getPlannedMealsByDate(Long date) {
        return plannedMealDao.getPlannedMealsByDate(date);
    }

    public Single<PlannedMeal> getPlannedMealByDateAndType(Long date, MealType mealType) {
        return plannedMealDao.getPlannedMealByDateAndType(date, mealType);
    }


    public Single<Boolean> isPlannedMealExists(Long date, MealType mealType) {
        return plannedMealDao.isPlannedMealExists(date, mealType);
    }

    public Observable<List<PlannedMeal>> getAllPlannedMeals() {
        return plannedMealDao.getAllPlannedMeals();
    }

    public void cleanupOldPlannedMeals() {
        long today = getStartOfDay(System.currentTimeMillis());
        plannedMealDao.deleteOldPlannedMeals(today);
    }


    public void deleteAllPlannedMeals() {
        plannedMealDao.deleteAllPlannedMeals();
    }

    public Single<Integer> getPlannedMealsCount() {
        long[] dateRange = getSevenDayRange();
        return plannedMealDao.getPlannedMealsCount(dateRange[0], dateRange[1]);
    }

    public Observable<List<PlannedMeal>> getPlannedMealsByType(MealType mealType) {
        long startDate = getStartOfDay(System.currentTimeMillis());
        return plannedMealDao.getPlannedMealsByType(mealType, startDate);
    }

    private void validateDateWithinSevenDays(Long timestamp) {
        long[] range = getSevenDayRange();
        if (timestamp < range[0] || timestamp >= range[1]) {
            throw new IllegalArgumentException("Planned meals can only be scheduled within the next 7 days");
        }
    }

    private long[] getSevenDayRange() {
        long startOfToday = getStartOfDay(System.currentTimeMillis());
        long endOfSevenDays = getStartOfDay(startOfToday + (MAX_PLANNING_DAYS * 24 * 60 * 60 * 1000L));
        return new long[]{startOfToday, endOfSevenDays};
    }

    public static long getStartOfDay(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }


    public static long getDateTimestamp(int daysFromToday) {
        if (daysFromToday < 0 || daysFromToday >= MAX_PLANNING_DAYS) {
            throw new IllegalArgumentException("Days from today must be between 0 and " + (MAX_PLANNING_DAYS - 1));
        }
        long today = getStartOfDay(System.currentTimeMillis());
        return today + (daysFromToday * 24 * 60 * 60 * 1000L);
    }
}