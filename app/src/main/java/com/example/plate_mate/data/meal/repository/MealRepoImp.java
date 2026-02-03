package com.example.plate_mate.data.meal.repository;

import com.example.plate_mate.data.meal.datasource.remote.MealRemoteDataSource;
import com.example.plate_mate.data.meal.model.InitialMealData;

import io.reactivex.rxjava3.core.Observable;

public class MealRepoImp implements MealRepository {

    private InitialMealData cachedSplashData;
    private final MealRemoteDataSource remoteDataSource;

    public MealRepoImp() {
        this.remoteDataSource = new MealRemoteDataSource();
    }

    @Override
    public Observable<InitialMealData> preloadInitialData() {
        return Observable.zip(
                remoteDataSource.listCategories(),
                remoteDataSource.listIngredients(),
                remoteDataSource.listAreas(),
                remoteDataSource.searchMealByFirstLetter("a"),
                (categories, ingredients, areas, meals) -> {
                    InitialMealData splashData = new InitialMealData(
                            categories,
                            ingredients,
                            areas,
                            meals
                    );
                    cachedSplashData = splashData;
                    return splashData;
                }
        );
    }

    @Override
    public InitialMealData getCachedSplashData() {
        return cachedSplashData;
    }
}