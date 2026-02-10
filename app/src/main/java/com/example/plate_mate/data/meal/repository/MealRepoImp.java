package com.example.plate_mate.data.meal.repository;

import android.content.Context;

import com.example.plate_mate.data.meal.datasource.local.FavoriteLocalDataStore;
import com.example.plate_mate.data.meal.datasource.local.MealSharedPrefManager;
import com.example.plate_mate.data.meal.datasource.local.PlannedMealLocalDataStore;
import com.example.plate_mate.data.meal.datasource.remote.MealRemoteDataSource;
import com.example.plate_mate.data.meal.model.AreaResponse;
import com.example.plate_mate.data.meal.model.CategorieListResponse;
import com.example.plate_mate.data.meal.model.IngredientResponse;
import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.model.MealResponse;
import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealRepoImp implements MealRepository {
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

    @Override
    public Single<MealResponse> getMealById(String id) {
        return remoteDataSource.getMealById(id).onErrorReturnItem(new MealResponse());
    }

    @Override
    public Completable insertFavorite(Meal favorite) {
        return Completable.fromAction(() -> favoriteLocalDataStore.insertFavorite(favorite))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<Meal>> getAllFavorites() {
        return favoriteLocalDataStore.getAllFavorites()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Meal> getFavoriteById(String mealId) {
        return Single.fromCallable(() -> favoriteLocalDataStore.getFavoriteById(mealId))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deleteFavorite(String mealId) {
        return Completable.fromAction(() -> favoriteLocalDataStore.deleteFavorite(mealId))
                .subscribeOn(Schedulers.io());
    }
    @Override
    public Completable insertPlannedMeal(PlannedMeal plannedMeal) {
        return Completable.fromAction(() -> plannedMealLocalDataStore.insertPlannedMeal(plannedMeal))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable insertPlannedMeals(List<PlannedMeal> plannedMeals) {
        return Completable.fromAction(() -> plannedMealLocalDataStore.insertPlannedMeals(plannedMeals))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable updatePlannedMeal(PlannedMeal plannedMeal) {
        return Completable.fromAction(() -> plannedMealLocalDataStore.updatePlannedMeal(plannedMeal))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deletePlannedMeal(PlannedMeal plannedMeal) {
        return Completable.fromAction(() -> plannedMealLocalDataStore.deletePlannedMeal(plannedMeal))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deletePlannedMealByDateAndType(Long date, MealType mealType) {
        return Completable.fromAction(() ->
                        plannedMealLocalDataStore.deletePlannedMealByDateAndType(date, mealType))
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<PlannedMeal>> getPlannedMealsForNextSevenDays() {
        return plannedMealLocalDataStore.getPlannedMealsForNextSevenDays()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<List<PlannedMeal>> getPlannedMealsByDate(Long date) {
        return plannedMealLocalDataStore.getPlannedMealsByDate(date)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<PlannedMeal> getPlannedMealByDateAndType(Long date, MealType mealType) {
        return plannedMealLocalDataStore.getPlannedMealByDateAndType(date, mealType)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Boolean> isPlannedMealExists(Long date, MealType mealType) {
        return plannedMealLocalDataStore.isPlannedMealExists(date, mealType)
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<PlannedMeal>> getAllPlannedMeals() {
        return plannedMealLocalDataStore.getAllPlannedMeals()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable cleanupOldPlannedMeals() {
        return Completable.fromAction(() -> plannedMealLocalDataStore.cleanupOldPlannedMeals())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Completable deleteAllPlannedMeals() {
        return Completable.fromAction(() -> plannedMealLocalDataStore.deleteAllPlannedMeals())
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Single<Integer> getPlannedMealsCount() {
        return plannedMealLocalDataStore.getPlannedMealsCount()
                .subscribeOn(Schedulers.io());
    }

    @Override
    public Observable<List<PlannedMeal>> getPlannedMealsByType(MealType mealType) {
        return plannedMealLocalDataStore.getPlannedMealsByType(mealType)
                .subscribeOn(Schedulers.io());
    }
}