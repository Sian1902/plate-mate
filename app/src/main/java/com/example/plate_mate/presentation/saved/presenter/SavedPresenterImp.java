package com.example.plate_mate.presentation.saved.presenter;

import android.content.Context;

import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.example.plate_mate.presentation.saved.view.SavedView;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SavedPresenterImp implements SavedPresenter {
    private final MealRepository mealRepo;
    private final SavedView savedView;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Set<String> favoriteMealIds = new HashSet<>();

    public SavedPresenterImp(Context context, SavedView savedView) {
        this.mealRepo = MealRepoImp.getInstance(context);
        this.savedView = savedView;
    }

    @Override
    public void loadFavorites() {
        // Subscribe to favorites observable to get real-time updates
        disposables.add(mealRepo.getAllFavorites()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(favorites -> {
                    if (savedView == null) return;

                    // Update favorite IDs set
                    favoriteMealIds.clear();
                    for (Meal meal : favorites) {
                        if (meal.getIdMeal() != null) {
                            favoriteMealIds.add(meal.getIdMeal());
                        }
                    }

                    // Show or hide empty state based on list
                    if (favorites.isEmpty()) {
                        savedView.showEmptyState();
                    } else {
                        savedView.hideEmptyState();
                        savedView.showFavorites(favorites);
                    }

                    // Update favorite states in adapter
                    savedView.updateFavorites(favoriteMealIds);

                }, error -> {
                    if (savedView != null) {
                        savedView.showError("Failed to load favorites: " + error.getMessage());
                    }
                }));
    }

    @Override
    public void toggleFavorite(Meal meal) {
        if (meal == null || meal.getIdMeal() == null) return;

        // Since this is the favorites screen, we only remove from favorites
        boolean isFavorite = favoriteMealIds.contains(meal.getIdMeal());

        if (isFavorite) {
            removeFavorite(meal.getIdMeal());
        }
    }

    private void removeFavorite(String mealId) {
        disposables.add(mealRepo.deleteFavorite(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    // The observable will automatically update the UI
                    // No need to manually update here as loadFavorites subscription handles it
                }, error -> {
                    if (savedView != null) {
                        savedView.showError("Failed to remove from favorites: " + error.getMessage());
                    }
                }));
    }

    @Override
    public void dispose() {
        disposables.clear();
    }
}