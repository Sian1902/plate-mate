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

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {
    public static final String PREF_NAME = "OnboardingPrefs";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    private static final String TAG = "SplashActivity";
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
        Log.d(TAG, "Onboarding not completed, preloading data...");
        preloadDataAndNavigate(OnboardingActivity.class);
    }

    private void preloadDataAndNavigateToMain() {
        Log.d(TAG, "User logged in, preloading data for main...");
        preloadDataAndNavigate(MainActivity.class);
    }

    private void preloadDataAndNavigateToAuth() {
        Log.d(TAG, "New user, preloading data for auth...");
        preloadDataAndNavigate(AuthActivity.class);
    }

    private void preloadDataAndNavigate(Class<?> targetActivity) {
        splashDisposable = splashPresenter.preloadData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .andThen(splashDelay().ignoreElements())
                .subscribe(
                        () -> {
                            navigateTo(targetActivity);
                        },
                        error -> {
                            Log.e(TAG, "Critical failure during preload", error);
                            navigateTo(targetActivity);
                        }
                );
    }
    private Observable<Long> splashDelay() {
        long elapsed = System.currentTimeMillis() - startTime;
        long remaining = Math.max(0, MIN_SPLASH_DURATION_MS - elapsed);
        return Observable.timer(remaining, TimeUnit.MILLISECONDS);
    }

    private void navigateTo(Class<?> targetActivity) {
        Intent intent = new Intent(SplashActivity.this, targetActivity);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }


    private void setupAnimation(LottieAnimationView anim, int brandColor) {
        anim.addValueCallback(new KeyPath("**"), LottieProperty.COLOR_FILTER, frameInfo -> new PorterDuffColorFilter(brandColor, PorterDuff.Mode.SRC_ATOP));
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashDisposable != null && !splashDisposable.isDisposed()) {
            splashDisposable.dispose();
        }
    }
}