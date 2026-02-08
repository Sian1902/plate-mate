package com.example.plate_mate.data.meal.repository;

import android.content.Context;
import com.example.plate_mate.data.meal.datasource.local.MealSharedPrefManager;
import com.example.plate_mate.data.meal.datasource.remote.MealRemoteDataSource;
import com.example.plate_mate.data.meal.model.AreaResponse;
import com.example.plate_mate.data.meal.model.CategorieListResponse;
import com.example.plate_mate.data.meal.model.IngredientResponse;
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
                remoteDataSource.listCategories().onErrorReturnItem(new CategorieListResponse()),
                remoteDataSource.listIngredients().onErrorReturnItem(new IngredientResponse()),
                remoteDataSource.listAreas().onErrorReturnItem(new AreaResponse()),
                remoteDataSource.searchMealByFirstLetter("a").onErrorReturnItem(new MealResponse()),
                remoteDataSource.getRandomMeal().onErrorReturnItem(new MealResponse()),
                InitialMealData::new
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
        return remoteDataSource.filterByCategory(category).onErrorReturnItem(new MealResponse());
    }

    @Override
    public Single<MealResponse> searchMealsByArea(String area) {
        return remoteDataSource.filterByArea(area).onErrorReturnItem(new MealResponse());
    }

    @Override
    public Single<MealResponse> searchMealsByIngredient(String ingredient) {
        return remoteDataSource.filterByIngredient(ingredient).onErrorReturnItem(new MealResponse());
    }

    @Override
    public Observable<MealResponse> SearchMealsByName(String name) {
        return remoteDataSource.searchMealByName(name).onErrorReturnItem(new MealResponse());
    }

    public Single<MealResponse> getMealById(String id) {
        return remoteDataSource.getMealById(id).onErrorReturnItem(new MealResponse());
    }
}