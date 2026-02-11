package com.example.plate_mate.presentation.planner.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Meal;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class MealSearchAdapter extends RecyclerView.Adapter<MealSearchAdapter.ViewHolder> {

    private final OnMealSelectedListener listener;
    private List<Meal> meals = new ArrayList<>();

    public MealSearchAdapter(OnMealSelectedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(meals.get(position));
    }

    @Override
    public int getItemCount() {
        return meals.size();
    }

    public void updateMeals(List<Meal> newMeals) {
        this.meals = newMeals != null ? newMeals : new ArrayList<>();
        notifyDataSetChanged();
    }

    public interface OnMealSelectedListener {
        void onMealSelected(Meal meal);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ShapeableImageView mealImage;
        private final TextView mealName;
        private final TextView mealCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mealImage = itemView.findViewById(R.id.search_meal_image);
            mealName = itemView.findViewById(R.id.search_meal_name);
            mealCategory = itemView.findViewById(R.id.search_meal_category);
        }

        public void bind(Meal meal) {
            mealName.setText(meal.getStrMeal());
            mealCategory.setText(meal.getStrCategory());

            Glide.with(itemView.getContext()).load(meal.getStrMealThumb()).placeholder(R.drawable.ic_launcher_background).error(R.drawable.chef).into(mealImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMealSelected(meal);
                }
            });
        }
    }
}