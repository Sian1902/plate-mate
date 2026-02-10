package com.example.plate_mate.presentation.home.presenter;

import android.content.Context;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.example.plate_mate.presentation.home.contract.HomeContract;

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

public class HomePresenterImp implements HomeContract.Presenter {
    private MealRepository mealRepo;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final CompositeDisposable filterDisposables = new CompositeDisposable();
    private Disposable updateDisposable;

    private final PublishSubject<String> searchSubject = PublishSubject.create();
    private HomeContract.View homeView;

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

    public HomePresenterImp(Context context, HomeContract.View homeView) {
        this.mealRepo = MealRepoImp.getInstance(context);
        this.homeView = homeView;
        setupSearchDebounce();
    }

    private void setupSearchDebounce() {
        disposables.add(searchSubject
                .debounce(400, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::fetchSearchMeals, throwable -> {}));
    }

    @Override
    public void loadHomeData() {
        disposables.add(mealRepo.getCachedSplashData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(data -> {
                    if (homeView == null) return;

                    if (data != null) {
                        if (data.getCategories() != null && data.getAreas() != null && data.getIngredients() != null) {
                            homeView.setFilterOptions(
                                    data.getCategories().getMeal() != null ? data.getCategories().getMeal() : new ArrayList<>(),
                                    data.getAreas().getMeals() != null ? data.getAreas().getMeals() : new ArrayList<>(),
                                    data.getIngredients().getMeals() != null ? data.getIngredients().getMeals() : new ArrayList<>()
                            );
                        }
                        if (data.getMeals() != null && data.getMeals().getMeals() != null) {
                            initialMeals = new ArrayList<>(data.getMeals().getMeals());
                        } else {
                            initialMeals = new ArrayList<>();
                        }

                        Meal heroMeal = null;
                        if (data.getRandomMeal() != null &&
                                data.getRandomMeal().getMeals() != null &&
                                !data.getRandomMeal().getMeals().isEmpty()) {
                            heroMeal = data.getRandomMeal().getMeals().get(0);
                        }
                        homeView.setupUi(initialMeals, heroMeal);

                        loadFavorites();
                    }
                }, e -> {
                    if (homeView != null) {
                        homeView.showError("Failed to load data: " + e.getMessage());
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
                        if (meal.getIdMeal() != null) {
                            favoriteMealIds.add(meal.getIdMeal());
                        }
                    }
                    homeView.updateFavorites(favoriteMealIds);
                }, e -> {
                    if (homeView != null) {
                        homeView.showError("Failed to load favorites: " + e.getMessage());
                    }
                }));
    }

    @Override
    public void toggleFavorite(Meal meal) {
        if (meal == null || meal.getIdMeal() == null) return;

        boolean isFavorite = favoriteMealIds.contains(meal.getIdMeal());

        if (isFavorite) {
            removeFavorite(meal.getIdMeal());
        } else {
            if (isMealComplete(meal)) {
                addFavorite(meal);
            } else {
                fetchCompleteMealAndAddToFavorites(meal.getIdMeal());
            }
        }
    }

    private boolean isMealComplete(Meal meal) {
        return meal.getStrInstructions() != null && !meal.getStrInstructions().trim().isEmpty();
    }

    private void fetchCompleteMealAndAddToFavorites(String mealId) {
        disposables.add(mealRepo.getMealById(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response != null && response.getMeals() != null && !response.getMeals().isEmpty()) {
                        Meal completeMeal = response.getMeals().get(0);
                        addFavorite(completeMeal);
                    } else {
                        if (homeView != null) {
                            homeView.showError("Failed to fetch meal details");
                        }
                    }
                }, e -> {
                    if (homeView != null) {
                        homeView.showError("Failed to add to favorites: " + e.getMessage());
                    }
                }));
    }

    private void addFavorite(Meal meal) {
        disposables.add(mealRepo.insertFavorite(meal)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    favoriteMealIds.add(meal.getIdMeal());
                    if (homeView != null) {
                        homeView.updateFavorites(favoriteMealIds);
                    }
                }, e -> {
                    if (homeView != null) {
                        homeView.showError("Failed to add to favorites: " + e.getMessage());
                    }
                }));
    }

    private void removeFavorite(String mealId) {
        disposables.add(mealRepo.deleteFavorite(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    favoriteMealIds.remove(mealId);
                    if (homeView != null) {
                        homeView.updateFavorites(favoriteMealIds);
                    }
                }, e -> {
                    if (homeView != null) {
                        homeView.showError("Failed to remove from favorites: " + e.getMessage());
                    }
                }));
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

        homeView.updateMealList(new ArrayList<>(initialMeals));
    }

    private void fetchCategoryMeals(String category) {
        filterDisposables.add(mealRepo.searchMealsByCategory(category)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    categoryResults = (res != null && res.getMeals() != null) ? res.getMeals() : new ArrayList<>();
                    updateResults();
                }, e -> {}));
    }

    private void fetchAreaMeals(String area) {
        filterDisposables.add(mealRepo.searchMealsByArea(area)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    areaResults = (res != null && res.getMeals() != null) ? res.getMeals() : new ArrayList<>();
                    updateResults();
                }, e -> {}));
    }

    private void fetchIngredientMeals(String ingredient) {
        filterDisposables.add(mealRepo.searchMealsByIngredient(ingredient)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    ingredientResults = (res != null && res.getMeals() != null) ? res.getMeals() : new ArrayList<>();
                    updateResults();
                }, e -> {}));
    }

    private void fetchSearchMeals(String query) {
        filterDisposables.add(mealRepo.SearchMealsByName(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> {
                    searchResults = (res != null && res.getMeals() != null) ? res.getMeals() : new ArrayList<>();
                    updateResults();
                }, e -> {}));
    }

    private void updateResults() {
        if (updateDisposable != null) updateDisposable.dispose();

        updateDisposable = Observable.fromCallable(() -> {
                    List<List<Meal>> active = new ArrayList<>();
                    if (currentCategory != null) active.add(categoryResults);
                    if (currentArea != null) active.add(areaResults);
                    if (currentIngredient != null) active.add(ingredientResults);
                    if (!currentSearchQuery.isEmpty()) active.add(searchResults);

                    if (active.isEmpty()) return initialMeals;
                    for (List<Meal> list : active) if (list.isEmpty()) return new ArrayList<Meal>();

                    Map<String, Meal> mealMap = new HashMap<>();
                    for (Meal m : active.get(0)) {
                        if (m.getIdMeal() != null) mealMap.put(m.getIdMeal(), m);
                    }

                    for (int i = 1; i < active.size(); i++) {
                        Set<String> nextIds = new HashSet<>();
                        for (Meal m : active.get(i)) nextIds.add(m.getIdMeal());
                        mealMap.keySet().retainAll(nextIds);
                    }
                    return new ArrayList<>(mealMap.values());
                }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(homeView::updateMealList, e -> {});
    }

    public void dispose() {
        disposables.clear();
        filterDisposables.clear();
        if (updateDisposable != null) updateDisposable.dispose();
    }
}