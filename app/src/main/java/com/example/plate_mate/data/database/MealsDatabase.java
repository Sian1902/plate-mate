package com.example.plate_mate.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.plate_mate.data.meal.datasource.local.FavoriteDao;
import com.example.plate_mate.data.meal.datasource.local.PlannedMealDao;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.model.PlannedMeal;

@Database(entities = {Meal.class, PlannedMeal.class}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class MealsDatabase extends RoomDatabase {
    private static volatile MealsDatabase INSTANCE;

    public abstract FavoriteDao favoriteDao();
    public abstract PlannedMealDao plannedMealDao();

    public static MealsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (MealsDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    MealsDatabase.class,
                                    "meals_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}