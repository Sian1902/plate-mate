package com.example.plate_mate.presentation.mealdetails.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.presentation.mealdetails.presenter.MealDetailsPresenter;
import com.google.android.material.card.MaterialCardView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class MealDetailsFragment extends Fragment implements MealDetailsView {

    private TextView tvTitle, tvCategory, tvIngredients, tvInstructions;
    private ImageView imgMeal;
    private ImageButton btnFavorite;
    private YouTubePlayerView youtubePlayerView;
    private MaterialCardView videoCard;
    private NavVisibilityCallback navVisibilityCallback;
    private MealDetailsPresenter presenter;
    private Meal currentMeal;
    private boolean isFavorite = false;

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
        tvCategory = view.findViewById(R.id.tvCategory);
        tvIngredients = view.findViewById(R.id.tvIngredients);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        imgMeal = view.findViewById(R.id.imgMeal);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        youtubePlayerView = view.findViewById(R.id.youtube_player_view);
        videoCard = view.findViewById(R.id.videoCard);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        btnFavorite.setOnClickListener(v -> toggleFavorite());

        getLifecycle().addObserver(youtubePlayerView);

        presenter = new MealDetailsPresenter(this, requireContext());

        if (getArguments() != null) {
            Meal meal = (Meal) getArguments().getSerializable("selected_meal");
            if (meal != null) {
                currentMeal = meal;
                presenter.checkIfFavorite(meal.getIdMeal());

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
        String firstIngredient = meal.getStrIngredient(1);

        boolean hasInstructions = instructions != null && !instructions.trim().isEmpty();
        boolean hasIngredients = firstIngredient != null && !firstIngredient.trim().isEmpty();

        return hasInstructions && hasIngredients;
    }

    private void toggleFavorite() {
        if (currentMeal == null) return;

        if (isFavorite) {
            presenter.removeFromFavorites(currentMeal.getIdMeal());
        } else {
            presenter.addToFavorites(currentMeal);
        }
    }

    @Override
    public void showMealDetails(Meal meal) {
        currentMeal = meal;
        tvTitle.setText(meal.getStrMeal());

        if (meal.getStrCategory() != null && meal.getStrArea() != null) {
            tvCategory.setText(meal.getStrCategory() + " • " + meal.getStrArea());
        } else if (meal.getStrCategory() != null) {
            tvCategory.setText(meal.getStrCategory());
        } else if (meal.getStrArea() != null) {
            tvCategory.setText(meal.getStrArea());
        }

        if (meal.getStrMealThumb() != null && !meal.getStrMealThumb().isEmpty()) {
            Glide.with(this)
                    .load(meal.getStrMealThumb())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .centerCrop()
                    .into(imgMeal);
        }

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
                videoCard.setVisibility(View.VISIBLE);
                youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                    @Override
                    public void onReady(@NonNull YouTubePlayer youtubePlayer) {
                        youtubePlayer.cueVideo(videoId, 0);
                    }
                });
            } else {
                videoCard.setVisibility(View.GONE);
            }
        } else {
            videoCard.setVisibility(View.GONE);
        }
    }

    @Override
    public void updateFavoriteStatus(boolean isFavorite) {
        this.isFavorite = isFavorite;
        btnFavorite.setImageResource(isFavorite ? R.drawable.favorite : R.drawable.outline_favorite_24);
    }

    @Override
    public void showFavoriteAdded() {
        Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showFavoriteRemoved() {
        Toast.makeText(getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
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