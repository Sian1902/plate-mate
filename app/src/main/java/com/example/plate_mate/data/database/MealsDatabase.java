package com.example.plate_mate.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.plate_mate.data.meal.datasource.local.FavoriteDao;
import com.example.plate_mate.data.meal.model.Meal;

@Database(entities = {Meal.class}, version = 1, exportSchema = false)
public abstract class MealsDatabase extends RoomDatabase {
    private static volatile MealsDatabase INSTANCE;
    public abstract FavoriteDao favoriteDao();


    public static MealsDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE= Room.databaseBuilder(
                    context.getApplicationContext(),
                    MealsDatabase.class,
                    "meals_database").build();
        }
        return INSTANCE;
    }
}
