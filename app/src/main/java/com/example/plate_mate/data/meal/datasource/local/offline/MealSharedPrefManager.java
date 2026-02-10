package com.example.plate_mate.data.meal.datasource.local.offline;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.plate_mate.data.meal.model.InitialMealData;
import com.google.gson.Gson;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class MealSharedPrefManager {

    private static final String PREF_NAME = "meal_prefs";
    private static final String INITIAL_DATA_KEY = "initial_meal_data";

    private static volatile MealSharedPrefManager instance;

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private MealSharedPrefManager(Context context) {
        sharedPreferences = context
                .getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static MealSharedPrefManager getInstance(Context context) {
        if (instance == null) {
            synchronized (MealSharedPrefManager.class) {
                if (instance == null) {
                    instance = new MealSharedPrefManager(context);
                }
            }
        }
        return instance;
    }
    public Completable saveInitialData(InitialMealData data) {
        return Completable.fromAction(() -> {
            String json = gson.toJson(data);
            sharedPreferences.edit()
                    .putString(INITIAL_DATA_KEY, json)
                    .apply();
        });
    }
    public Single<InitialMealData> getCachedInitialData() {
        return Single.fromCallable(() -> {
            String json = sharedPreferences.getString(INITIAL_DATA_KEY, null);
            if (json == null) {
                return new InitialMealData();
            }
            return gson.fromJson(json, InitialMealData.class);
        });
    }

}
