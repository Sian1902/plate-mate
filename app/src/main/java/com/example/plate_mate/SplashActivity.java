package com.example.plate_mate;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 seconds
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the splash theme before calling super
        setTheme(R.style.Theme_PlateMate_Splash);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        progressBar = findViewById(R.id.progressBar);

        // Start the fake loading progress animation
        startLoadingAnimation();

        // Transition to Main Activity after duration
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close splash so back button doesn't return here
        }, SPLASH_DURATION);
    }

    private void startLoadingAnimation() {
        // Simple runnable to increment progress
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            int progress = 0;
            @Override
            public void run() {
                if (progress <= 100) {
                    progressBar.setProgress(progress);
                    progress++;
                    handler.postDelayed(this, 20); // Adjust speed here
                }
            }
        }, 100);
    }
}