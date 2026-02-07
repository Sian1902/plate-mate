package com.example.plate_mate.presentation.splash.view;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.example.plate_mate.AuthActivity;
import com.example.plate_mate.MainActivity;
import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.datasource.remote.MealRemoteDataSource;
import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.example.plate_mate.presentation.splash.presenter.SplashPresenter;
import com.example.plate_mate.presentation.splash.presenter.SplashPresenterImp;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";

    private Disposable splashDisposable;
    private SplashPresenter splashPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_PlateMate_Splash);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        LottieAnimationView blurAnimTop = findViewById(R.id.blurAnimationTop);
        LottieAnimationView blurAnimBottom = findViewById(R.id.blurAnimationBottom);
        LottieAnimationView progressBar = findViewById(R.id.lottieProgressBar);

        int brandColor = ContextCompat.getColor(this, R.color.primary);
        setupAnimation(blurAnimTop, brandColor);
        setupAnimation(blurAnimBottom, brandColor);
        setupAnimation(progressBar, brandColor);

        splashPresenter = new SplashPresenterImp(getApplicationContext());

        // Load data and wait for it
        Log.d(TAG, "Starting to preload data...");

        splashDisposable = splashPresenter.preloadData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::onDataLoaded,
                        this::onDataLoadError
                );
    }

    private void onDataLoaded(InitialMealData data) {
        Log.d(TAG, "Data loaded successfully!");

        if (data.getCategories() != null && data.getCategories().getMeal() != null) {
            Log.d(TAG, "Categories count: " + data.getCategories().getMeal().size());
            data.getCategories().getMeal().forEach(category ->
                    Log.d(TAG, "Category: " + category.getStrCategory())
            );
        } else {
            Log.w(TAG, "Categories data is null");
        }

        if (data.getIngredients() != null && data.getIngredients().getMeals() != null) {
            Log.d(TAG, "Ingredients count: " + data.getIngredients().getMeals().size());
            data.getIngredients().getMeals().forEach(ingredient ->
                    Log.d(TAG, "Ingredient: " + ingredient.getStrIngredient())
            );
        } else {
            Log.w(TAG, "Ingredients data is null");
        }
        if (data.getAreas() != null && data.getAreas().getMeals() != null) {
            Log.d(TAG, "Areas count: " + data.getAreas().getMeals().size());
            data.getAreas().getMeals().forEach(area ->
                    Log.d(TAG, "Area: " + area.getStrArea())
            );
        } else {
            Log.w(TAG, "Areas data is null");
        }

        if (data.getMeals() != null && data.getMeals().getMeals() != null) {
            Log.d(TAG, "Meals count: " + data.getMeals().getMeals().size());
            data.getMeals().getMeals().forEach(meal ->
                    Log.d(TAG, "Meal: " + meal.getStrMeal())
            );
        } else {
            Log.w(TAG, "Meals data is null");
        }
        goToAuth();
    }

    private void onDataLoadError(Throwable error) {
        Log.e(TAG, "Error loading data: " + error.getMessage(), error);

        goToAuth();
    }

    private void setupAnimation(LottieAnimationView anim, int brandColor) {
        anim.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(brandColor, PorterDuff.Mode.SRC_ATOP)
        );
    }

    private void goToMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    private void goToAuth() {
        Intent intent = new Intent(SplashActivity.this, AuthActivity.class);
        startActivity(intent);

        // Professional fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Finish splash so the user can't go back to it
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashDisposable != null && !splashDisposable.isDisposed()) {
            splashDisposable.dispose();
        }
    }
}