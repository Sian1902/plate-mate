package com.example.plate_mate.presentation.home.presenter;

import android.content.Context;
import android.util.Log;
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
import java.util.concurrent.TimeUnit;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.PublishSubject;

public class HomePresenterImp implements HomePresenter {
    private static final String TAG = "HomePresenterImp";
    private MealRepository mealRepo;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final CompositeDisposable filterDisposables = new CompositeDisposable();
    private Disposable updateDisposable;
    private final PublishSubject<String> searchSubject = PublishSubject.create();
    private HomeView homeView;
    private List<Meal> initialMeals = new ArrayList<>();
    private List<Meal> categoryResults = new ArrayList<>();
    private List<Meal> areaResults = new ArrayList<>();
    private List<Meal> ingredientResults = new ArrayList<>();
    private List<Meal> searchResults = new ArrayList<>();
    private Set<String> favoriteMealIds = new HashSet<>();
    private String currentCategory = null;
    private String currentArea = null;
    private String currentIngredient = null;
    private String currentSearchQuery = "";

    public HomePresenterImp(Context context, HomeView homeView) {
        this.mealRepo = MealRepoImp.getInstance(context);
        this.homeView = homeView;
        setupSearchDebounce();
    }

    private void setupSearchDebounce() {
        disposables.add(searchSubject
                .debounce(400, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::fetchSearchMeals, throwable -> {
                    Log.e(TAG, "Search debounce error", throwable);
                }));
    }

    @Override
    public void loadHomeData() {
        Log.d(TAG, "Loading home data from cache...");

        disposables.add(mealRepo.getCachedSplashData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    Log.d(TAG, "Cached data received: " + (data != null ? "success" : "null"));

                    if (homeView == null) {
                        Log.w(TAG, "HomeView is null, cannot update UI");
                        return;
                    }

                    if (data != null) {
                        // Set filter options
                        List categories = (data.getCategories() != null && data.getCategories().getMeal() != null)
                                ? data.getCategories().getMeal()
                                : new ArrayList<>();
                        List areas = (data.getAreas() != null && data.getAreas().getMeals() != null)
                                ? data.getAreas().getMeals()
                                : new ArrayList<>();
                        List ingredients = (data.getIngredients() != null && data.getIngredients().getMeals() != null)
                                ? data.getIngredients().getMeals()
                                : new ArrayList<>();

                        Log.d(TAG, "Filter options - Categories: " + categories.size() +
                                ", Areas: " + areas.size() +
                                ", Ingredients: " + ingredients.size());

                        homeView.setFilterOptions(categories, areas, ingredients);

                        // Set initial meals
                        if (data.getMeals() != null && data.getMeals().getMeals() != null) {
                            initialMeals = new ArrayList<>(data.getMeals().getMeals());
                            Log.d(TAG, "Initial meals loaded: " + initialMeals.size());
                        } else {
                            initialMeals = new ArrayList<>();
                            Log.w(TAG, "No initial meals in cached data");
                        }

                        // Get hero meal
                        Meal heroMeal = null;
                        if (data.getRandomMeal() != null &&
                                data.getRandomMeal().getMeals() != null &&
                                !data.getRandomMeal().getMeals().isEmpty()) {
                            heroMeal = data.getRandomMeal().getMeals().get(0);
                            Log.d(TAG, "Hero meal: " + (heroMeal != null ? heroMeal.getStrMeal() : "null"));
                        } else {
                            Log.w(TAG, "No hero meal in cached data");
                        }

                        homeView.setupUi(initialMeals, heroMeal);
                        loadFavorites();
                    } else {
                        Log.w(TAG, "Cached data is null, showing empty UI");
                        homeView.setupUi(new ArrayList<>(), null);
                        homeView.showError("No offline data available. Please connect to the internet.");
                    }
                }, e -> {
                    Log.e(TAG, "Error loading cached data", e);
                    if (homeView != null) {
                        homeView.setupUi(new ArrayList<>(), null);
                        homeView.showError("Failed to load cached data: " + e.getMessage());
                    }
                }));
    }

    @Override
    public void loadFavorites() {
        disposables.add(mealRepo.getAllFavorites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favorites -> {
                    if (homeView == null) return;
                    favoriteMealIds.clear();
                    for (Meal meal : favorites) {
                        if (meal.getIdMeal() != null) favoriteMealIds.add(meal.getIdMeal());
                    }
                    Log.d(TAG, "Favorites loaded: " + favoriteMealIds.size());
                    homeView.updateFavorites(favoriteMealIds);
                }, e -> {
                    Log.e(TAG, "Error loading favorites", e);
                }));
    }

    @Override
    public void toggleFavorite(Meal meal) {
        if (meal == null || meal.getIdMeal() == null) return;
        if (favoriteMealIds.contains(meal.getIdMeal())) {
            disposables.add(mealRepo.deleteFavorite(meal.getIdMeal())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        favoriteMealIds.remove(meal.getIdMeal());
                        if (homeView != null) homeView.updateFavorites(favoriteMealIds);
                    }, e -> {
                        Log.e(TAG, "Error removing favorite", e);
                    }));
        } else {
            disposables.add(mealRepo.insertFavorite(meal)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                        favoriteMealIds.add(meal.getIdMeal());
                        if (homeView != null) homeView.updateFavorites(favoriteMealIds);
                    }, e -> {
                        Log.e(TAG, "Error adding favorite", e);
                    }));
        }
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
            searchSubject.onNext(currentSearchQuery);
        }
    }

    @Override
    public void clearAllFilters() {
        filterDisposables.clear();
        if (updateDisposable != null) updateDisposable.dispose();
        currentCategory = null;
        currentArea = null;
        currentIngredient = null;
        currentSearchQuery = "";
        categoryResults.clear();
        areaResults.clear();
        ingredientResults.clear();
        searchResults.clear();
        if (homeView != null) homeView.updateMealList(new ArrayList<>(initialMeals));
    }

    private void fetchCategoryMeals(String category) {
        filterDisposables.add(mealRepo.searchMealsByCategory(category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    categoryResults = res.getMeals() != null ? res.getMeals() : new ArrayList<>();
                    Log.d(TAG, "Category results: " + categoryResults.size());
                    updateResults();
                }, e -> {
                    Log.e(TAG, "Error fetching category meals", e);
                    categoryResults = new ArrayList<>();
                    updateResults();
                }));
    }

    private void fetchAreaMeals(String area) {
        filterDisposables.add(mealRepo.searchMealsByArea(area)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    areaResults = res.getMeals() != null ? res.getMeals() : new ArrayList<>();
                    Log.d(TAG, "Area results: " + areaResults.size());
                    updateResults();
                }, e -> {
                    Log.e(TAG, "Error fetching area meals", e);
                    areaResults = new ArrayList<>();
                    updateResults();
                }));
    }

    private void fetchIngredientMeals(String ingredient) {
        filterDisposables.add(mealRepo.searchMealsByIngredient(ingredient)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    ingredientResults = res.getMeals() != null ? res.getMeals() : new ArrayList<>();
                    Log.d(TAG, "Ingredient results: " + ingredientResults.size());
                    updateResults();
                }, e -> {
                    Log.e(TAG, "Error fetching ingredient meals", e);
                    ingredientResults = new ArrayList<>();
                    updateResults();
                }));
    }

    private void fetchSearchMeals(String query) {
        filterDisposables.add(mealRepo.SearchMealsByName(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    searchResults = res.getMeals() != null ? res.getMeals() : new ArrayList<>();
                    Log.d(TAG, "Search results for '" + query + "': " + searchResults.size());
                    updateResults();
                }, e -> {
                    Log.e(TAG, "Error searching meals", e);
                    searchResults = new ArrayList<>();
                    updateResults();
                }));
    }

    private void updateResults() {
        if (updateDisposable != null) updateDisposable.dispose();
        updateDisposable = Observable.fromCallable(() -> {
                    List<List<Meal>> active = new ArrayList<>();
                    if (currentCategory != null) active.add(categoryResults);
                    if (currentArea != null) active.add(areaResults);
                    if (currentIngredient != null) active.add(ingredientResults);
                    if (!currentSearchQuery.isEmpty()) active.add(searchResults);

                    if (active.isEmpty()) {
                        Log.d(TAG, "No active filters, returning initial meals: " + initialMeals.size());
                        return initialMeals;
                    }

                    for (List<Meal> list : active) {
                        if (list == null || list.isEmpty()) {
                            Log.d(TAG, "One of the filter results is empty, returning empty list");
                            return new ArrayList<Meal>();
                        }
                    }

                    Map<String, Meal> mealMap = new HashMap<>();
                    for (Meal m : active.get(0)) {
                        if (m.getIdMeal() != null) mealMap.put(m.getIdMeal(), m);
                    }
                    for (int i = 1; i < active.size(); i++) {
                        Set<String> nextIds = new HashSet<>();
                        for (Meal m : active.get(i)) nextIds.add(m.getIdMeal());
                        mealMap.keySet().retainAll(nextIds);
                    }

                    List<Meal> result = new ArrayList<>(mealMap.values());
                    Log.d(TAG, "Filtered results: " + result.size());
                    return result;
                }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(meals -> {
                    if (homeView != null) homeView.updateMealList(meals);
                }, e -> {
                    Log.e(TAG, "Error updating results", e);
                });
    }

    public void dispose() {
        disposables.clear();
        filterDisposables.clear();
        if (updateDisposable != null) updateDisposable.dispose();
    }
}