package com.example.plate_mate.presentation.home.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Meal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private final OnMealClickListener listener;
    private final OnFavoriteClickListener favoriteListener;
    private final Set<String> favoriteMealIds = new HashSet<>();
    private List<Meal> mealList;

    public MealAdapter(List<Meal> mealList, OnMealClickListener listener, OnFavoriteClickListener favoriteListener) {
        this.mealList = mealList != null ? mealList : new ArrayList<>();
        this.listener = listener;
        this.favoriteListener = favoriteListener;
    }

    public void updateMeals(List<Meal> newMeals) {
        this.mealList = newMeals != null ? newMeals : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void updateFavorites(Set<String> favoriteIds) {
        this.favoriteMealIds.clear();
        if (favoriteIds != null) {
            this.favoriteMealIds.addAll(favoriteIds);
        }
        notifyDataSetChanged();
    }

    public void toggleFavorite(String mealId) {
        if (favoriteMealIds.contains(mealId)) {
            favoriteMealIds.remove(mealId);
        } else {
            favoriteMealIds.add(mealId);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        boolean isFavorite = favoriteMealIds.contains(meal.getIdMeal());
        holder.bind(meal, isFavorite, listener, favoriteListener);
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public interface OnMealClickListener {
        void onMealClick(Meal meal);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Meal meal, boolean isFavorite);
    }

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivMealImage;
        private final TextView tvMealName;
        private final ImageView ivFavorite;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMealImage = itemView.findViewById(R.id.ivMealThumbnail);
            tvMealName = itemView.findViewById(R.id.tvMealTitle);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }

        public void bind(Meal meal, boolean isFavorite, OnMealClickListener listener, OnFavoriteClickListener favoriteListener) {
            tvMealName.setText(meal.getStrMeal());

            Glide.with(itemView.getContext()).load(meal.getStrMealThumb()).into(ivMealImage);

            if (isFavorite) {
                ivFavorite.setImageResource(R.drawable.favorite);
            } else {
                ivFavorite.setImageResource(R.drawable.outline_favorite_24);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMealClick(meal);
                }
            });

            ivFavorite.setOnClickListener(v -> {
                if (favoriteListener != null) {
                    favoriteListener.onFavoriteClick(meal, isFavorite);
                }
            });
        }
    }
}