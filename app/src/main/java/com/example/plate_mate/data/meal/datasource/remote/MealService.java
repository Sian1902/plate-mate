package com.example.plate_mate.data.meal.datasource.remote;

import com.example.plate_mate.data.meal.model.AreaResponse;
import com.example.plate_mate.data.meal.model.CategorieListResponse;
import com.example.plate_mate.data.meal.model.CategoryResponse;
import com.example.plate_mate.data.meal.model.IngredientResponse;
import com.example.plate_mate.data.meal.model.MealResponse;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealService {
    @GET("search.php")
    Observable<MealResponse> searchMealByName(@Query("s") String name);

    @GET("search.php")
    Observable<MealResponse> searchMealByFirstLetter(@Query("f") String letter);

    @GET("lookup.php")
    Observable<MealResponse> getMealById(@Query("i") String id);

    @GET("random.php")
    Observable<MealResponse> getRandomMeal();

    @GET("categories.php")
    Observable<CategoryResponse> getCategories();

    @GET("list.php?c=list")
    Observable<CategorieListResponse> listCategories();

    @GET("list.php?a=list")
    Observable<AreaResponse> listAreas();

    @GET("list.php?i=list")
    Observable<IngredientResponse> listIngredients();

    // Changed to Single for filter methods
    @GET("filter.php")
    Single<MealResponse> filterByIngredient(@Query("i") String ingredient);

    @GET("filter.php")
    Single<MealResponse> filterByCategory(@Query("c") String category);

    @GET("filter.php")
    Single<MealResponse> filterByArea(@Query("a") String area);
}