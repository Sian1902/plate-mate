package com.example.plate_mate.presentation.splash.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.example.plate_mate.AuthActivity;
import com.example.plate_mate.MainActivity;
import com.example.plate_mate.R;
import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.repo.AuthRepo;
import com.example.plate_mate.data.auth.repo.AuthRepoImp;
import com.example.plate_mate.data.meal.model.InitialMealData;
import com.example.plate_mate.presentation.onboarding.view.OnboardingActivity;
import com.example.plate_mate.presentation.splash.presenter.SplashPresenter;
import com.example.plate_mate.presentation.splash.presenter.SplashPresenterImp;
import com.example.plate_mate.util.DarkModeHelper;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {
    private static final String TAG = "SplashActivity";
    public static final String PREF_NAME = "OnboardingPrefs";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final long MIN_SPLASH_DURATION_MS = 1800;
    private Disposable splashDisposable;
    private SplashPresenter splashPresenter;
    private AuthRepo authRepo;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AuthRemoteDataSource remoteDataSource = new AuthRemoteDataSource();
        AuthPrefManager prefManager = AuthPrefManager.getInstance(getApplicationContext());
        authRepo = new AuthRepoImp(remoteDataSource, prefManager);

        DarkModeHelper.applyDarkMode(getApplicationContext());

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

        startTime = System.currentTimeMillis();

        splashPresenter = new SplashPresenterImp(getApplicationContext());

        // Check onboarding first
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        boolean onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false);

        if (!onboardingCompleted) {
            Log.d(TAG, "Onboarding not completed, preloading data before onboarding...");
            goToOnboarding();
            return;
        }

        if (isUserLoggedIn()) {
            Log.d(TAG, "User is already logged in, skipping auth screen");
            preloadDataAndNavigateToMain();
        } else {
            Log.d(TAG, "User is not logged in, will show auth screen");
            preloadDataAndNavigateToAuth();
        }
    }

    private boolean isUserLoggedIn() {
        return authRepo.isUserLoggedIn();
    }

    private void goToOnboarding() {
        Log.d(TAG, "Starting data preload for onboarding...");

        splashDisposable = splashPresenter.preloadData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            Log.d(TAG, "Preload completed successfully for onboarding");
                            logDataInfo(data);

                            // Wait for minimum splash duration, then navigate
                            long elapsed = System.currentTimeMillis() - startTime;
                            long delay = Math.max(0, MIN_SPLASH_DURATION_MS - elapsed);

                            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                                finish();
                            }, delay);
                        },
                        error -> {
                            Log.e(TAG, "Preload failed during onboarding", error);

                            // Even on error, navigate after minimum duration
                            long elapsed = System.currentTimeMillis() - startTime;
                            long delay = Math.max(0, MIN_SPLASH_DURATION_MS - elapsed);

                            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                                startActivity(new Intent(SplashActivity.this, OnboardingActivity.class));
                                finish();
                            }, delay);
                        }
                );
    }

    private void preloadDataAndNavigateToMain() {
        Log.d(TAG, "Starting to preload data for logged-in user...");

        splashDisposable = splashPresenter.preloadData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::onDataLoadedAndGoToMain,
                        this::onDataLoadErrorAndGoToMain
                );
    }

    private void preloadDataAndNavigateToAuth() {
        Log.d(TAG, "Starting to preload data for new user...");

        splashDisposable = splashPresenter.preloadData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::onDataLoaded,
                        this::onDataLoadError
                );
    }

    private void onDataLoadedAndGoToMain(InitialMealData data) {
        Log.d(TAG, "Data loaded successfully for logged-in user!");
        logDataInfo(data);
        goToMain();
    }

    private void onDataLoadErrorAndGoToMain(Throwable error) {
        Log.e(TAG, "Error loading data for logged-in user: " + error.getMessage(), error);
        goToMain();
    }

    private void onDataLoaded(InitialMealData data) {
        Log.d(TAG, "Data loaded successfully!");
        logDataInfo(data);
        goToAuth();
    }

    private void onDataLoadError(Throwable error) {
        Log.e(TAG, "Error loading data: " + error.getMessage(), error);
        goToAuth();
    }

    private void logDataInfo(InitialMealData data) {
        if (data == null) {
            Log.w(TAG, "Data is null");
            return;
        }

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

        if (data.getRandomMeal() != null && data.getRandomMeal().getMeals() != null) {
            Log.d(TAG, "Random meal count: " + data.getRandomMeal().getMeals().size());
        } else {
            Log.w(TAG, "Random meal data is null");
        }
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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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