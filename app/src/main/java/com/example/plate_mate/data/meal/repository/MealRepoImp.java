package com.example.plate_mate.data.meal.repository;

import android.content.Context;

import com.example.plate_mate.data.meal.datasource.local.MealSharedPrefManager;
import com.example.plate_mate.data.meal.datasource.remote.MealRemoteDataSource;
import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.data.meal.model.MealResponse;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;

public class MealRepoImp implements MealRepository {
    private static volatile MealRepoImp instance;
    private final MealRemoteDataSource remoteDataSource;
    private final MealSharedPrefManager dataStoreManager;

    private MealRepoImp(Context context) {
        this.remoteDataSource = new MealRemoteDataSource();
        this.dataStoreManager = MealSharedPrefManager.getInstance(context);
    }

    public static MealRepoImp getInstance(Context context) {
        if (instance == null) {
            synchronized (MealRepoImp.class) {
                if (instance == null) {
                    instance = new MealRepoImp(context);
                }
            }
        }
        return instance;
    }

    @Override
    public Observable<InitialMealData> preloadInitialData() {
        return Observable.zip(
                remoteDataSource.listCategories(),
                remoteDataSource.listIngredients(),
                remoteDataSource.listAreas(),
                remoteDataSource.searchMealByFirstLetter("a"),
                remoteDataSource.getRandomMeal(),
                (categories, ingredients, areas, meals, randomMeal) -> {
                    InitialMealData splashData = new InitialMealData(
                            categories, ingredients, areas, meals, randomMeal
                    );
                    return splashData;
                }
        ).flatMap(splashData ->
                dataStoreManager.saveInitialData(splashData)
                        .andThen(Observable.just(splashData))
        );
    }

    @Override
    public Single<InitialMealData> getCachedSplashData() {
        return dataStoreManager.getCachedInitialData();
    }

    @Override
    public Single<MealResponse> searchMealsByCategory(String category) {
        return remoteDataSource.filterByCategory(category);
    }

    @Override
    public Single<MealResponse> searchMealsByArea(String area) {
        return remoteDataSource.filterByArea(area);
    }

    @Override
    public Single<MealResponse> searchMealsByIngredient(String ingredient) {
        return remoteDataSource.filterByIngredient(ingredient);
    }
}