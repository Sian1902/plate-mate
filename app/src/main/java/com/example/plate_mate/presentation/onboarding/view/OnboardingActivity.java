package com.example.plate_mate.presentation.onboarding.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.plate_mate.AuthActivity;
import com.example.plate_mate.R;
import com.example.plate_mate.presentation.onboarding.presenter.OnboardingPresenter;
import com.example.plate_mate.presentation.onboarding.presenter.OnboardingPresenterImp;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class OnboardingActivity extends AppCompatActivity implements OnboardingView{

    private ViewPager2 viewPager;
    private DotsIndicator dotsIndicator;
    private Button btnNext;
    private ImageButton btnBack;
    private TextView tvSkip;

    private OnboardingPresenter presenter;
    private OnboardingAdapter adapter;

    int currentPosition = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);


        findViews();
        presenter = new OnboardingPresenterImp(this);
        presenter.setup();

        adapter = new OnboardingAdapter(presenter.getOnboardingItems());
        viewPager.setAdapter(adapter);
        dotsIndicator.attachTo(viewPager);

        btnBack.setVisibility(View.INVISIBLE);
        tvSkip.setVisibility(View.VISIBLE);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                presenter.onPageSelected(position);
            }
        });

        btnNext.setOnClickListener(v -> presenter.onNextClicked());
        tvSkip.setOnClickListener(v -> presenter.onSkipClicked());
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> presenter.onBackClicked());
        }
    }

    private void findViews() {
        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        btnNext = findViewById(R.id.btnNext);
        tvSkip = findViewById(R.id.tvSkip);
        btnBack = findViewById(R.id.back_arrow_onboarding);
    }

    @Override
    public void updateButtonText(int text) {
        btnNext.setText(text);
    }

    @Override
    public void navigateToAuth() {
        // Mark onboarding as done
        SharedPreferences prefs = getSharedPreferences("OnboardingPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("onboarding_completed", true);
        editor.apply(); // or .commit() – apply() is async and usually preferred
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);

        // Professional fade transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Finish splash so the user can't go back to it
        finish();
    }

    @Override
    public void setCurrentPosition(int position) {
        currentPosition = position;
    }

    @Override
    public int getCurrentPosition() {
        return viewPager.getCurrentItem();
    }

    @Override
    public void goToNextPage() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
    }

    @Override
    public void goToPreviousPage() {
        viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
    }
    @Override
    public void showBackButton() {
        if (btnBack != null) {
            btnBack.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void hideSkip() {
        if (tvSkip != null) {
            tvSkip.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideBackButton() {
        if (btnBack != null) {
            btnBack.setVisibility(View.GONE);  // or INVISIBLE
        }
    }

    @Override
    public void showSkip() {
        if (tvSkip != null) {
            tvSkip.setVisibility(View.VISIBLE);
        }
    }
}