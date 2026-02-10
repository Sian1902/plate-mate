package com.example.plate_mate.data.meal.datasource.local.favorit;

import android.content.Context;

import com.example.plate_mate.data.database.MealsDatabase;
import com.example.plate_mate.data.meal.model.Meal;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

public class FavoriteLocalDataStore {
    private final FavoriteDao favoriteDao;

    public FavoriteLocalDataStore(Context context) {
        favoriteDao = MealsDatabase.getInstance(context).favoriteDao();
    }
    public void insertFavorite(Meal favorite) {
        favoriteDao.insertFavorite(favorite);
    }
    public Observable<List<Meal>> getAllFavorites() {
        return favoriteDao.getAllFavorites();
    }
    public Meal getFavoriteById(String mealId) {
        return favoriteDao.getFavoriteById(mealId);
    }

    public void deleteFavorite(String mealId) {
        favoriteDao.deleteFavorite(mealId);
    }

}
