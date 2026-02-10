package com.example.plate_mate.data.meal.datasource.local.favorit;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.plate_mate.data.meal.model.Meal;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertFavorite(Meal favorite);

    @Query("SELECT * FROM favorites")
    Observable<List<Meal>> getAllFavorites();

    @Query("SELECT * FROM favorites WHERE idMeal = :mealId")
    Meal getFavoriteById(String mealId);

    @Query("DELETE FROM favorites WHERE idMeal = :mealId")
    void deleteFavorite(String mealId);
}
