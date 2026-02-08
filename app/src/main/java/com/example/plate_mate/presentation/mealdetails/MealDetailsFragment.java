package com.example.plate_mate.presentation.mealdetails;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Meal;
import com.google.android.material.card.MaterialCardView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;

public class MealDetailsFragment extends Fragment {

    private static final String TAG = "MealDetailsFragment";
    private TextView tvTitle, tvIngredients, tvInstructions;
    private YouTubePlayerView youtubePlayerView;
    private ImageButton btnBack;
    private MaterialCardView videoCard;
    private boolean hasYouTubeError = false;

    // Callback interface for activity
    private NavVisibilityCallback navVisibilityCallback;

    // Define the callback interface
    public interface NavVisibilityCallback {
        void setNavigationVisibility(boolean isVisible);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Ensure the activity implements our callback interface
        if (context instanceof NavVisibilityCallback) {
            navVisibilityCallback = (NavVisibilityCallback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement NavVisibilityCallback");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide navigation in activity
        if (navVisibilityCallback != null) {
            navVisibilityCallback.setNavigationVisibility(false);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");

        // Initialize views from your XML design
        tvTitle = view.findViewById(R.id.tvMealTitle);
        tvIngredients = view.findViewById(R.id.tvIngredients);
        tvInstructions = view.findViewById(R.id.tvInstructions);
        youtubePlayerView = view.findViewById(R.id.youtube_player_view);
        btnBack = view.findViewById(R.id.btnBack);

        // Get parent card view
        if (youtubePlayerView != null && youtubePlayerView.getParent() instanceof MaterialCardView) {
            videoCard = (MaterialCardView) youtubePlayerView.getParent();
        }

        btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        getLifecycle().addObserver(youtubePlayerView);

        if (getArguments() != null) {
            Meal meal = (Meal) getArguments().getSerializable("selected_meal");
            if (meal != null) {
                Log.d(TAG, "Meal received: " + meal.getStrMeal());
                bindMealData(meal);
            } else {
                Log.e(TAG, "Meal is null in arguments!");
            }
        } else {
            Log.e(TAG, "Arguments are null!");
        }
    }

    private void bindMealData(Meal meal) {
        Log.d(TAG, "bindMealData called for: " + meal.getStrMeal());

        tvTitle.setText(meal.getStrMeal());
        tvInstructions.setText(meal.getStrInstructions());

        // Format the ingredients into a single string from the 20 provided fields
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            String ingredient = meal.getStrIngredient(i);
            String measure = meal.getStrMeasure(i);

            if (ingredient != null && !ingredient.trim().isEmpty()) {
                builder.append("• ")
                        .append(measure != null ? measure.trim() : "")
                        .append(" ")
                        .append(ingredient.trim())
                        .append("\n");
            }
        }
        tvIngredients.setText(builder.toString().trim());

        // Initialize Video Player
        String youtubeUrl = meal.getStrYoutube();
        Log.d(TAG, "YouTube URL: " + youtubeUrl);

        if (youtubeUrl != null && !youtubeUrl.isEmpty()) {
            String videoId = extractVideoId(youtubeUrl);
            Log.d(TAG, "Extracted video ID: " + videoId);

            if (videoId != null && !videoId.isEmpty()) {
                setupYouTubePlayer(videoId, youtubeUrl);
            } else {
                Log.w(TAG, "Video ID extraction failed");
                hideYouTubePlayer();
            }
        } else {
            Log.w(TAG, "No YouTube URL provided");
            hideYouTubePlayer();
        }
    }

    private void setupYouTubePlayer(String videoId, String fullUrl) {
        try {
            youtubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youtubePlayer) {
                    Log.d(TAG, "YouTube player ready, cueing video: " + videoId);
                    youtubePlayer.cueVideo(videoId, 0);
                }

                @Override
                public void onError(@NonNull YouTubePlayer youTubePlayer,
                                    @NonNull com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants.PlayerError error) {
                    Log.e(TAG, "YouTube player error: " + error.name() + " - Using fallback");
                    hasYouTubeError = true;
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error setting up YouTube player", e);
            hasYouTubeError = true;
        }
    }

    private void hideYouTubePlayer() {
        if (videoCard != null) {
            videoCard.setVisibility(View.GONE);
        }
    }

    private String extractVideoId(String youtubeUrl) {
        if (youtubeUrl == null || youtubeUrl.isEmpty()) {
            Log.w(TAG, "YouTube URL is null or empty");
            return null;
        }

        try {
            Log.d(TAG, "Attempting to extract video ID from: " + youtubeUrl);

            if (youtubeUrl.contains("v=")) {
                // Format: https://www.youtube.com/watch?v=VIDEO_ID
                String[] parts = youtubeUrl.split("v=");
                if (parts.length > 1) {
                    String videoId = parts[1].split("&")[0];
                    Log.d(TAG, "Extracted video ID (v= format): " + videoId);
                    return videoId;
                }
            } else if (youtubeUrl.contains("youtu.be/")) {
                // Format: https://youtu.be/VIDEO_ID
                String[] parts = youtubeUrl.split("youtu.be/");
                if (parts.length > 1) {
                    String videoId = parts[1].split("\\?")[0];
                    Log.d(TAG, "Extracted video ID (youtu.be format): " + videoId);
                    return videoId;
                }
            } else if (youtubeUrl.contains("embed/")) {
                // Format: https://www.youtube.com/embed/VIDEO_ID
                String[] parts = youtubeUrl.split("embed/");
                if (parts.length > 1) {
                    String videoId = parts[1].split("\\?")[0];
                    Log.d(TAG, "Extracted video ID (embed format): " + videoId);
                    return videoId;
                }
            }

            Log.w(TAG, "No matching YouTube URL format found");
        } catch (Exception e) {
            Log.e(TAG, "Error extracting video ID", e);
        }
        return null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (youtubePlayerView != null && !hasYouTubeError) {
            youtubePlayerView.release();
            Log.d(TAG, "YouTube player released");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Show navigation when fragment is destroyed
        if (navVisibilityCallback != null) {
            navVisibilityCallback.setNavigationVisibility(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        navVisibilityCallback = null;
    }
}