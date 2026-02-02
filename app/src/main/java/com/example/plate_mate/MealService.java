package com.example.plate_mate;

import io.reactivex.rxjava3.core.Observable;
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
    Observable<MealResponse> listCategories();
    @GET("list.php?a=list")
    Observable<MealResponse> listAreas();
    @GET("list.php?i=list")
    Observable<MealResponse> listIngredients();
    @GET("filter.php")
    Observable<MealResponse> filterByIngredient(@Query("i") String ingredient);
    @GET("filter.php")
    Observable<MealResponse> filterByCategory(@Query("c") String category);
    @GET("filter.php")
    Observable<MealResponse> filterByArea(@Query("a") String area);
}
