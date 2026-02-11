package com.example.plate_mate.presentation.mealdetails.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.presentation.mealdetails.presenter.MealDetailsPresenter;
import com.google.android.material.card.MaterialCardView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class MealDetailsFragment extends Fragment implements MealDetailsView {

    private TextView tvTitle, tvIngredients, tvInstructions;
    private YouTubePlayerView youtubePlayerView;
    private MaterialCardView videoCard;
    private NavVisibilityCallback navVisibilityCallback;
    private MealDetailsPresenter presenter;

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
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        if (youtubePlayerView != null && youtubePlayerView.getParent() instanceof MaterialCardView) {
            videoCard = (MaterialCardView) youtubePlayerView.getParent();
        }

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        getLifecycle().addObserver(youtubePlayerView);

        presenter = new MealDetailsPresenter(this, requireContext());

        if (getArguments() != null) {
            Meal meal = (Meal) getArguments().getSerializable("selected_meal");
            if (meal != null) {
                if (hasFullData(meal)) {
                    showMealDetails(meal);
                } else {
                    presenter.fetchMealDetails(meal.getIdMeal());
                }
            }
        }
    }

    private boolean hasFullData(Meal meal) {
        String instructions = meal.getStrInstructions();
        String firstIngredient = meal.getStrIngredient(1); // Using the specific getter if available, or getStrIngredient(1)

        boolean hasInstructions = instructions != null && !instructions.trim().isEmpty();
        boolean hasIngredients = firstIngredient != null && !firstIngredient.trim().isEmpty();

        return hasInstructions && hasIngredients;
    }

    @Override
    public void showMealDetails(Meal meal) {
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
            if (videoId != null) {
                youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youtubePlayer) {
                        youtubePlayer.cueVideo(videoId, 0);
                    }
                });
            } else {
                if (videoCard != null) videoCard.setVisibility(View.GONE);
            }
        } else {
            if (videoCard != null) videoCard.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private String extractVideoId(String youtubeUrl) {
        if (youtubeUrl.contains("v=")) return youtubeUrl.split("v=")[1].split("&")[0];
        if (youtubeUrl.contains("youtu.be/"))
            return youtubeUrl.split("youtu.be/")[1].split("\\?")[0];
        return null;
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }

    public interface NavVisibilityCallback {
        void setNavigationVisibility(boolean isVisible);
    }
}