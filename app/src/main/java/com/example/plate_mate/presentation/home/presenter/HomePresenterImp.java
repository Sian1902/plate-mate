package com.example.plate_mate.presentation.home.presenter;

import android.content.Context;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.example.plate_mate.presentation.home.view.HomeView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomePresenterImp implements HomePresenter {
    private MealRepository mealRepo;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private HomeView homeView;

    private List<Meal> initialMeals = new ArrayList<>();
    private List<Meal> categoryResults = new ArrayList<>();
    private List<Meal> areaResults = new ArrayList<>();
    private List<Meal> ingredientResults = new ArrayList<>();
    private List<Meal> searchResults = new ArrayList<>();

    private String currentCategory = null;
    private String currentArea = null;
    private String currentIngredient = null;
    private String currentSearchQuery = "";

    public HomePresenterImp(Context context, HomeView homeView) {
        this.mealRepo = MealRepoImp.getInstance(context);
        this.homeView = homeView;
    }

    @Override
    public void loadHomeData() {
        disposables.add(mealRepo.getCachedSplashData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (data != null) {
                        if (data.getCategories() != null && data.getAreas() != null && data.getIngredients() != null) {
                            homeView.setFilterOptions(data.getCategories().getMeal(), data.getAreas().getMeals(), data.getIngredients().getMeals());
                        }
                        if (data.getMeals() != null) {
                            initialMeals = new ArrayList<>(data.getMeals().getMeals());
                            Meal hero = (data.getRandomMeal() != null && !data.getRandomMeal().getMeals().isEmpty()) ? data.getRandomMeal().getMeals().get(0) : null;
                            homeView.setupUi(initialMeals, hero);
                        }
                    }
                }, throwable -> homeView.showError("Network Error")));
    }

    @Override
    public void filterMeals(String category, String area, String ingredient) {
        if (category != null) {
            currentCategory = category;
            fetchCategoryMeals(category);
        }
        if (area != null) {
            currentArea = area;
            fetchAreaMeals(area);
        }
        if (ingredient != null) {
            currentIngredient = ingredient;
            fetchIngredientMeals(ingredient);
        }
    }

    @Override
    public void searchMeals(String query) {
        currentSearchQuery = (query == null) ? "" : query.trim();
        if (currentSearchQuery.isEmpty()) {
            searchResults.clear();
            updateResults();
        } else {
            fetchSearchMeals(currentSearchQuery);
        }
    }

    @Override
    public void clearAllFilters() {
        homeView.updateMealList(initialMeals);
        currentCategory = null;
        currentArea = null;
        currentIngredient = null;
        currentSearchQuery = "";
        categoryResults.clear();
        areaResults.clear();
        ingredientResults.clear();
        searchResults.clear();
    }

    private void fetchCategoryMeals(String category) {
        disposables.add(mealRepo.searchMealsByCategory(category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    categoryResults = (res != null && res.getMeals() != null) ? res.getMeals() : new ArrayList<>();
                    updateResults();
                }, e -> homeView.showError("Category fetch failed")));
    }

    private void fetchAreaMeals(String area) {
        disposables.add(mealRepo.searchMealsByArea(area)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    areaResults = (res != null && res.getMeals() != null) ? res.getMeals() : new ArrayList<>();
                    updateResults();
                }, e -> homeView.showError("Area fetch failed")));
    }

    private void fetchIngredientMeals(String ingredient) {
        disposables.add(mealRepo.searchMealsByIngredient(ingredient)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    ingredientResults = (res != null && res.getMeals() != null) ? res.getMeals() : new ArrayList<>();
                    updateResults();
                }, e -> homeView.showError("Ingredient fetch failed")));
    }

    private void fetchSearchMeals(String query) {
        disposables.add(mealRepo.SearchMealsByName(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    searchResults = (res != null && res.getMeals() != null) ? res.getMeals() : new ArrayList<>();
                    updateResults();
                }, e -> homeView.showError("Search failed")));
    }

    private void updateResults() {
        disposables.add(Observable.fromCallable(() -> {
                    List<List<Meal>> active = new ArrayList<>();
                    if (currentCategory != null) active.add(categoryResults);
                    if (currentArea != null) active.add(areaResults);
                    if (currentIngredient != null) active.add(ingredientResults);
                    if (!currentSearchQuery.isEmpty()) active.add(searchResults);

                    if (active.isEmpty()) return initialMeals;

                    for (List<Meal> list : active) if (list.isEmpty()) return new ArrayList<Meal>();

                    Map<String, Meal> mealMap = new HashMap<>();
                    for (Meal m : active.get(0)) mealMap.put(m.getIdMeal(), m);

                    for (int i = 1; i < active.size(); i++) {
                        Set<String> nextIds = new HashSet<>();
                        for (Meal m : active.get(i)) nextIds.add(m.getIdMeal());
                        mealMap.keySet().retainAll(nextIds);
                    }
                    return new ArrayList<>(mealMap.values());
                }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homeView::updateMealList, e -> homeView.showError("Filter error")));
    }

    public void dispose() { disposables.clear(); }
}