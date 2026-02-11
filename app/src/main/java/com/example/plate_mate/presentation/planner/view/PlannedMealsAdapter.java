package com.example.plate_mate.presentation.planner.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.PlannedMeal;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

class PlannedMealsAdapter extends RecyclerView.Adapter<PlannedMealsAdapter.ViewHolder> {

    private List<PlannedMeal> plannedMeals;
    private final OnMealActionListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.getDefault());

    public interface OnMealActionListener {
        void onMealClick(PlannedMeal plannedMeal);
        void onSwapClick(PlannedMeal plannedMeal);
        void onEditClick(PlannedMeal plannedMeal);
    }

    public PlannedMealsAdapter(List<PlannedMeal> plannedMeals, OnMealActionListener listener) {
        this.plannedMeals = plannedMeals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meal_plan_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlannedMeal plannedMeal = plannedMeals.get(position);
        holder.bind(plannedMeal);
    }

    @Override
    public int getItemCount() {
        return plannedMeals != null ? plannedMeals.size() : 0;
    }

    public void updateMeals(List<PlannedMeal> newMeals) {
        this.plannedMeals = newMeals;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mealType;
        private final TextView mealName;
        private final TextView mealTime;
        private final MaterialButton swapBtn;
        private final ShapeableImageView mealImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mealType = itemView.findViewById(R.id.meal_type);
            mealName = itemView.findViewById(R.id.meal_name);
            mealTime = itemView.findViewById(R.id.meal_time);
            swapBtn = itemView.findViewById(R.id.swap_btn);
            mealImage = itemView.findViewById(R.id.meal_image);
        }

        public void bind(PlannedMeal plannedMeal) {
            if (plannedMeal == null) return;
            String mealTypeText = formatMealType(plannedMeal.getMealType());
            mealType.setText(mealTypeText);
            if (plannedMeal.getMeal() != null) {
                mealName.setText(plannedMeal.getMeal().getStrMeal());
                mealTime.setText("30 min");

                Glide.with(itemView.getContext())
                        .load(plannedMeal.getMeal().getStrMealThumb())
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.chef)
                        .into(mealImage);
            } else {
                mealName.setText("No meal planned");
                mealTime.setText("");
                mealImage.setImageResource(R.drawable.ic_launcher_background);
            }

            // Set up click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null && plannedMeal.getMeal() != null) {
                    listener.onMealClick(plannedMeal);
                }
            });

            swapBtn.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSwapClick(plannedMeal);
                }
            });
        }

        private String formatMealType(com.example.plate_mate.data.meal.model.MealType mealType) {
            if (mealType == null) return "Meal";

            switch (mealType) {
                case BREAKFAST:
                    return "Breakfast";
                case LUNCH:
                    return "Lunch";
                case DINNER:
                    return "Dinner";

                default:
                    return mealType.name();
            }
        }
    }
}