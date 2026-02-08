package com.example.plate_mate.presentation.mealdetails;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.google.android.material.card.MaterialCardView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealDetailsFragment extends Fragment {

    private static final String TAG = "MealDetailsFragment";
    private TextView tvTitle, tvIngredients, tvInstructions;
    private YouTubePlayerView youtubePlayerView;
    private ImageButton btnBack;
    private MaterialCardView videoCard;

    private final CompositeDisposable disposables = new CompositeDisposable();
    private NavVisibilityCallback navVisibilityCallback;

    public interface NavVisibilityCallback {
        void setNavigationVisibility(boolean isVisible);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof NavVisibilityCallback) {
            navVisibilityCallback = (NavVisibilityCallback) context;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTitle = view.findViewById(R.id.tvMealTitle);
        tvIngredients = view.findViewById(R.id.tvIngredients);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        youtubePlayerView = view.findViewById(R.id.youtube_player_view);
        btnBack = view.findViewById(R.id.btnBack);

        if (youtubePlayerView != null && youtubePlayerView.getParent() instanceof MaterialCardView) {
            videoCard = (MaterialCardView) youtubePlayerView.getParent();
        }

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        getLifecycle().addObserver(youtubePlayerView);

        if (getArguments() != null) {
            Meal meal = (Meal) getArguments().getSerializable("selected_meal");
            if (meal != null) {
                fetchCompleteMealData(meal.getIdMeal());
            }
        }
    }

    private void fetchCompleteMealData(String mealId) {

        disposables.add(MealRepoImp.getInstance(requireContext())
                .getMealById(mealId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getMeals() != null && !response.getMeals().isEmpty()) {
                        bindMealData(response.getMeals().get(0));
                    } else {
                        Toast.makeText(getContext(), "Failed to load details", Toast.LENGTH_SHORT).show();
                    }
                }, throwable -> {
                    Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                }));
    }

    private void bindMealData(Meal meal) {
        tvTitle.setText(meal.getStrMeal());
        tvInstructions.setText(meal.getStrInstructions());

        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            String ingredient = meal.getStrIngredient(i);
            String measure = meal.getStrMeasure(i);
            if (ingredient != null && !ingredient.trim().isEmpty()) {
                builder.append("• ").append(measure != null ? measure.trim() : "").append(" ").append(ingredient.trim()).append("\n");
            }
        }
        tvIngredients.setText(builder.toString().trim());

        String youtubeUrl = meal.getStrYoutube();
        if (youtubeUrl != null && !youtubeUrl.isEmpty()) {
            String videoId = extractVideoId(youtubeUrl);
            if (videoId != null) setupYouTubePlayer(videoId);
            else hideYouTubePlayer();
        } else {
            hideYouTubePlayer();
        }
    }

    private void setupYouTubePlayer(String videoId) {
        youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youtubePlayer) {
                youtubePlayer.cueVideo(videoId, 0);
            }
        });
    }

    private void hideYouTubePlayer() {
        if (videoCard != null) videoCard.setVisibility(View.GONE);
    }

    private String extractVideoId(String youtubeUrl) {
        if (youtubeUrl.contains("v=")) return youtubeUrl.split("v=")[1].split("&")[0];
        if (youtubeUrl.contains("youtu.be/")) return youtubeUrl.split("youtu.be/")[1].split("\\?")[0];
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        disposables.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navVisibilityCallback != null) navVisibilityCallback.setNavigationVisibility(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (navVisibilityCallback != null) navVisibilityCallback.setNavigationVisibility(true);
    }
}