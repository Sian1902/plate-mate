package com.example.plate_mate.data.meal.repository;

import android.content.Context;
import android.util.Log;

import com.example.plate_mate.data.meal.datasource.local.FavoriteLocalDataStore;
import com.example.plate_mate.data.meal.datasource.local.MealSharedPrefManager;
import com.example.plate_mate.data.meal.datasource.local.PlannedMealLocalDataStore;
import com.example.plate_mate.data.meal.datasource.remote.MealRemoteDataSource;
import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.model.MealResponse;
import com.example.plate_mate.data.meal.model.PlannedMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealRepoImp implements MealRepository {
    private static final String TAG = "MealRepoImp";
    private static volatile MealRepoImp instance;
    private final MealRemoteDataSource remoteDataSource;
    private final MealSharedPrefManager dataStoreManager;
    private final FavoriteLocalDataStore favoriteLocalDataStore;
    private final PlannedMealLocalDataStore plannedMealLocalDataStore;

    private MealRepoImp(Context context) {
        this.remoteDataSource = new MealRemoteDataSource();
        this.dataStoreManager = MealSharedPrefManager.getInstance(context);
        this.favoriteLocalDataStore = new FavoriteLocalDataStore(context);
        this.plannedMealLocalDataStore = new PlannedMealLocalDataStore(context);
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
    public Completable preloadInitialData() {
        Log.d(TAG, "Starting to preload initial data...");

        return Observable.zip(remoteDataSource.listCategories(), remoteDataSource.listIngredients(), remoteDataSource.listAreas(), remoteDataSource.searchMealByFirstLetter("a"), remoteDataSource.getRandomMeal(), InitialMealData::new).flatMap(splashData -> {
            if (isValidData(splashData)) {
                return dataStoreManager.saveInitialData(splashData).andThen(Observable.just(splashData));
            } else {
                return dataStoreManager.getCachedInitialData().toObservable();
            }
        }).doOnNext(data -> Log.d(TAG, "Initial data loaded and cached")).ignoreElements().onErrorComplete();
    }

    private boolean isValidData(InitialMealData data) {
        if (data == null) {
            return false;
        }

        boolean hasCategories = data.getCategories() != null && data.getCategories().getMeal() != null && !data.getCategories().getMeal().isEmpty();

        boolean hasMeals = data.getMeals() != null && data.getMeals().getMeals() != null && !data.getMeals().getMeals().isEmpty();

        boolean hasAreas = data.getAreas() != null && data.getAreas().getMeals() != null && !data.getAreas().getMeals().isEmpty();

        boolean isValid = hasCategories && hasMeals && hasAreas;

        Log.d(TAG, "Data validation - Categories: " + hasCategories + ", Meals: " + hasMeals + ", Areas: " + hasAreas + " -> Valid: " + isValid);

        return isValid;
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

    @Override
    public Single<MealResponse> getMealById(String id) {
        return remoteDataSource.getMealById(id).onErrorReturnItem(new MealResponse());
    }

    @Override
    public Completable insertFavorite(Meal favorite) {
        return Completable.fromAction(() -> favoriteLocalDataStore.insertFavorite(favorite)).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Meal>> getAllFavorites() {
        return favoriteLocalDataStore.getAllFavorites().subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deleteFavorite(String mealId) {
        return Completable.fromAction(() -> favoriteLocalDataStore.deleteFavorite(mealId)).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable insertPlannedMeal(PlannedMeal plannedMeal) {
        return Completable.fromAction(() -> plannedMealLocalDataStore.insertPlannedMeal(plannedMeal)).subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<PlannedMeal>> getPlannedMealsForNextSevenDays() {
        return plannedMealLocalDataStore.getPlannedMealsForNextSevenDays().subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<PlannedMeal>> getPlannedMealsByDate(Long date) {
        return plannedMealLocalDataStore.getPlannedMealsByDate(date).subscribeOn(Schedulers.io());
    }


    @Override
    public Observable<List<PlannedMeal>> getAllPlannedMeals() {
        return plannedMealLocalDataStore.getAllPlannedMeals().subscribeOn(Schedulers.io());
    }

    @Override
    public Completable cleanupOldPlannedMeals() {
        return Completable.fromAction(() -> plannedMealLocalDataStore.cleanupOldPlannedMeals()).subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deleteAllPlannedMeals() {
        return Completable.fromAction(() -> plannedMealLocalDataStore.deleteAllPlannedMeals()).subscribeOn(Schedulers.io());
    }


}