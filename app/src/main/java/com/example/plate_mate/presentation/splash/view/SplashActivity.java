package com.example.plate_mate.presentation;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieProperty;
import com.airbnb.lottie.model.KeyPath;
import com.example.plate_mate.MainActivity;
import com.example.plate_mate.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;

public class SplashActivity extends AppCompatActivity {
    private Disposable splashDisposable;

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

        splashDisposable = Observable.timer(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> {
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                });

    }
    private void setupAnimation(LottieAnimationView anim, int brandColor){
        anim.addValueCallback(
                new KeyPath("**"),
                LottieProperty.COLOR_FILTER,
                frameInfo -> new PorterDuffColorFilter(brandColor, PorterDuff.Mode.SRC_ATOP)
        );
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (splashDisposable != null && !splashDisposable.isDisposed()) {
            splashDisposable.dispose();
        }
    }
}