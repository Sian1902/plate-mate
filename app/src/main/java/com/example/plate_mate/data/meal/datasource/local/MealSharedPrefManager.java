package com.example.plate_mate.data.meal.datasource.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.plate_mate.data.meal.model.InitialMealData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public class MealSharedPrefManager {

    private static final String TAG = "MealSharedPrefManager";
    private static final String PREF_NAME = "meal_prefs";
    private static final String INITIAL_DATA_KEY = "initial_meal_data";

    private static volatile MealSharedPrefManager instance;

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private MealSharedPrefManager(Context context) {
        sharedPreferences = context
                .getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new GsonBuilder()
                .setLenient()
                .create();
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
            try {
                Log.d(TAG, "Saving initial data...");

                if (data == null) {
                    Log.w(TAG, "Attempted to save null data");
                    return;
                }
                logDataInfo(data);

                String json = gson.toJson(data);
                Log.d(TAG, "JSON length: " + json.length() + " characters");

                boolean success = sharedPreferences.edit()
                        .putString(INITIAL_DATA_KEY, json)
                        .commit(); // Changed from apply() to commit()

                if (success) {
                    Log.d(TAG, "Initial data saved successfully");

                    // Verify the save
                    String savedJson = sharedPreferences.getString(INITIAL_DATA_KEY, null);
                    if (savedJson != null && savedJson.length() > 0) {
                        Log.d(TAG, "Verification: Data exists in SharedPreferences, length: " + savedJson.length());
                    } else {
                        Log.e(TAG, "Verification failed: Data not found in SharedPreferences!");
                    }
                } else {
                    Log.e(TAG, "Failed to save initial data to SharedPreferences");
                    throw new Exception("SharedPreferences commit failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error saving initial data", e);
                throw new RuntimeException("Failed to save initial data", e);
            }
        });
    }

    public Single<InitialMealData> getCachedInitialData() {
        return Single.fromCallable(() -> {
            try {
                Log.d(TAG, "Retrieving cached initial data...");

                String json = sharedPreferences.getString(INITIAL_DATA_KEY, null);

                if (json == null || json.isEmpty()) {
                    Log.w(TAG, "No cached data found in SharedPreferences");
                    return new InitialMealData();
                }

                Log.d(TAG, "Found cached data, JSON length: " + json.length());

                InitialMealData data = gson.fromJson(json, InitialMealData.class);

                if (data == null) {
                    Log.w(TAG, "Parsed data is null");
                    return new InitialMealData();
                }

                logDataInfo(data);

                return data;
            } catch (Exception e) {
                Log.e(TAG, "Error retrieving cached data", e);
                return new InitialMealData();
            }
        });
    }

    private void logDataInfo(InitialMealData data) {
        if (data == null) {
            Log.w(TAG, "Data is null");
            return;
        }

        int categoriesCount = (data.getCategories() != null && data.getCategories().getMeal() != null)
                ? data.getCategories().getMeal().size() : 0;
        int areasCount = (data.getAreas() != null && data.getAreas().getMeals() != null)
                ? data.getAreas().getMeals().size() : 0;
        int ingredientsCount = (data.getIngredients() != null && data.getIngredients().getMeals() != null)
                ? data.getIngredients().getMeals().size() : 0;
        int mealsCount = (data.getMeals() != null && data.getMeals().getMeals() != null)
                ? data.getMeals().getMeals().size() : 0;
        int randomMealCount = (data.getRandomMeal() != null && data.getRandomMeal().getMeals() != null)
                ? data.getRandomMeal().getMeals().size() : 0;

        Log.d(TAG, "Data summary - Categories: " + categoriesCount +
                ", Areas: " + areasCount +
                ", Ingredients: " + ingredientsCount +
                ", Meals: " + mealsCount +
                ", Random Meals: " + randomMealCount);
    }


}