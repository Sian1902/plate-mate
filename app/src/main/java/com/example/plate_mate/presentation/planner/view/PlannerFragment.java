package com.example.plate_mate.presentation.planner.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.datasource.local.PlannedMealLocalDataStore;
import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.presentation.planner.presenter.PlannerPresenterImp;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlannerFragment extends Fragment implements PlannerView {

    private final View[] dayViews = new View[7];
    private final TextView[] dayNameTextViews = new TextView[7];
    private final TextView[] dayNumberTextViews = new TextView[7];
    private final SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private final SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
    private final long[] weekTimestamps = new long[7];
    private final Map<Long, Boolean> daysWithMeals = new HashMap<>();
    private PlannerPresenterImp presenter;
    private TextView monthYearTextView;
    private ImageButton backArrow;
    private ImageButton rightArrow;
    private ConstraintLayout breakfastCard;
    private ConstraintLayout lunchCard;
    private ConstraintLayout dinnerCard;
    private TextView breakfastMealType;
    private TextView breakfastMealName;
    private TextView breakfastMealTime;
    private MaterialButton breakfastSwapBtn;
    private ShapeableImageView breakfastMealImage;
    private TextView lunchMealType;
    private TextView lunchMealName;
    private TextView lunchMealTime;
    private MaterialButton lunchSwapBtn;
    private ShapeableImageView lunchMealImage;
    private TextView dinnerMealType;
    private TextView dinnerMealName;
    private TextView dinnerMealTime;
    private MaterialButton dinnerSwapBtn;
    private ShapeableImageView dinnerMealImage;
    private int currentDayIndex = 0;
    private PlannedMeal currentBreakfast;
    private PlannedMeal currentLunch;
    private PlannedMeal currentDinner;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new PlannerPresenterImp(MealRepoImp.getInstance(requireContext()));
        initializeWeekTimestamps();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_planner, container, false);

        initViews(view);
        setupListeners();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter.attachView(this);

        updateMonthYear();
        updateDayItems();
        selectDay(0);

        presenter.cleanupOldMeals();
        loadWeekMeals();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }

    private void initViews(View view) {
        monthYearTextView = view.findViewById(R.id.month_year);
        backArrow = view.findViewById(R.id.back_arrow);
        rightArrow = view.findViewById(R.id.right_arrow);

        dayViews[0] = view.findViewById(R.id.day1);
        dayViews[1] = view.findViewById(R.id.day2);
        dayViews[2] = view.findViewById(R.id.day3);
        dayViews[3] = view.findViewById(R.id.day4);
        dayViews[4] = view.findViewById(R.id.day5);
        dayViews[5] = view.findViewById(R.id.day6);
        dayViews[6] = view.findViewById(R.id.day7);

        for (int i = 0; i < 7; i++) {
            dayNameTextViews[i] = dayViews[i].findViewById(R.id.day_name);
            dayNumberTextViews[i] = dayViews[i].findViewById(R.id.day_number);
        }

        breakfastCard = view.findViewById(R.id.breakfast_card);
        lunchCard = view.findViewById(R.id.lunch_card);
        dinnerCard = view.findViewById(R.id.dinner_card);

        breakfastMealType = breakfastCard.findViewById(R.id.meal_type);
        breakfastMealName = breakfastCard.findViewById(R.id.meal_name);
        breakfastMealTime = breakfastCard.findViewById(R.id.meal_time);
        breakfastSwapBtn = breakfastCard.findViewById(R.id.swap_btn);
        breakfastMealImage = breakfastCard.findViewById(R.id.meal_image);

        lunchMealType = lunchCard.findViewById(R.id.meal_type);
        lunchMealName = lunchCard.findViewById(R.id.meal_name);
        lunchMealTime = lunchCard.findViewById(R.id.meal_time);
        lunchSwapBtn = lunchCard.findViewById(R.id.swap_btn);
        lunchMealImage = lunchCard.findViewById(R.id.meal_image);

        dinnerMealType = dinnerCard.findViewById(R.id.meal_type);
        dinnerMealName = dinnerCard.findViewById(R.id.meal_name);
        dinnerMealTime = dinnerCard.findViewById(R.id.meal_time);
        dinnerSwapBtn = dinnerCard.findViewById(R.id.swap_btn);
        dinnerMealImage = dinnerCard.findViewById(R.id.meal_image);

        breakfastMealType.setText("Breakfast");
        lunchMealType.setText("Lunch");
        dinnerMealType.setText("Dinner");
    }

    private void setupListeners() {
        backArrow.setEnabled(false);
        backArrow.setAlpha(0.5f);
        rightArrow.setEnabled(false);
        rightArrow.setAlpha(0.5f);

        for (int i = 0; i < 7; i++) {
            final int dayIndex = i;
            dayViews[i].setOnClickListener(v -> selectDay(dayIndex));
        }

        breakfastSwapBtn.setOnClickListener(v -> onAddOrSwapMeal(MealType.BREAKFAST));
        breakfastCard.setOnClickListener(v -> onAddOrSwapMeal(MealType.BREAKFAST));

        lunchSwapBtn.setOnClickListener(v -> onAddOrSwapMeal(MealType.LUNCH));
        lunchCard.setOnClickListener(v -> onAddOrSwapMeal(MealType.LUNCH));

        dinnerSwapBtn.setOnClickListener(v -> onAddOrSwapMeal(MealType.DINNER));
        dinnerCard.setOnClickListener(v -> onAddOrSwapMeal(MealType.DINNER));

    }

    private void initializeWeekTimestamps() {
        for (int i = 0; i < 7; i++) {
            weekTimestamps[i] = PlannedMealLocalDataStore.getDateTimestamp(i);
        }
    }

    private void updateMonthYear() {
        Date date = new Date(weekTimestamps[0]);
        monthYearTextView.setText(monthYearFormat.format(date));
    }

    private void updateDayItems() {
        for (int i = 0; i < 7; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(weekTimestamps[i]);

            String dayName = dayNameFormat.format(calendar.getTime()).toUpperCase();
            dayNameTextViews[i].setText(dayName);

            int dayNumber = calendar.get(Calendar.DAY_OF_MONTH);
            dayNumberTextViews[i].setText(String.valueOf(dayNumber));
        }
    }

    private void selectDay(int dayIndex) {
        currentDayIndex = dayIndex;

        updateDayHighlights();

        long selectedDate = weekTimestamps[dayIndex];
        presenter.loadPlannedMealsForDate(selectedDate);
    }

    private void updateDayHighlights() {
        for (int i = 0; i < 7; i++) {
            boolean isSelected = (i == currentDayIndex);
            boolean hasMeals = daysWithMeals.getOrDefault(weekTimestamps[i], false);

            if (isSelected) {
                dayViews[i].setBackgroundResource(R.drawable.day_item_bg);
                dayNameTextViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
                dayNumberTextViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.primary));
            } else if (hasMeals) {
                dayViews[i].setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_light));
                dayNameTextViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
                dayNumberTextViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            } else {
                dayViews[i].setBackgroundResource(R.drawable.day_item_bg);
                dayNameTextViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_tertiary));
                dayNumberTextViews[i].setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
            }
        }
    }

    private void loadWeekMeals() {
        presenter.loadPlannedMealsForNextSevenDays();
    }

    private void onAddOrSwapMeal(MealType mealType) {
        MealSearchDialog dialog = new MealSearchDialog(requireContext(), MealRepoImp.getInstance(requireContext()), weekTimestamps[currentDayIndex], mealType, () -> {
            selectDay(currentDayIndex);
            loadWeekMeals();
        });
        dialog.show();
    }


    @Override
    public void showLoading() {
    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void showPlannedMeals(List<PlannedMeal> plannedMeals) {

        daysWithMeals.clear();
        for (PlannedMeal meal : plannedMeals) {
            daysWithMeals.put(meal.getDate(), true);
        }
        updateDayHighlights();
    }

    @Override
    public void showPlannedMealsForDate(Long date, List<PlannedMeal> meals) {

        currentBreakfast = null;
        currentLunch = null;
        currentDinner = null;

        resetMealCardToAdd(breakfastCard, breakfastMealName, breakfastMealTime, breakfastMealImage, breakfastSwapBtn, "Add Breakfast");
        resetMealCardToAdd(lunchCard, lunchMealName, lunchMealTime, lunchMealImage, lunchSwapBtn, "Add Lunch");
        resetMealCardToAdd(dinnerCard, dinnerMealName, dinnerMealTime, dinnerMealImage, dinnerSwapBtn, "Add Dinner");

        for (PlannedMeal plannedMeal : meals) {
            if (plannedMeal.getMeal() == null) continue;

            switch (plannedMeal.getMealType()) {
                case BREAKFAST:
                    currentBreakfast = plannedMeal;
                    populateMealCard(breakfastMealName, breakfastMealTime, breakfastMealImage, breakfastSwapBtn, plannedMeal);
                    break;
                case LUNCH:
                    currentLunch = plannedMeal;
                    populateMealCard(lunchMealName, lunchMealTime, lunchMealImage, lunchSwapBtn, plannedMeal);
                    break;
                case DINNER:
                    currentDinner = plannedMeal;
                    populateMealCard(dinnerMealName, dinnerMealTime, dinnerMealImage, dinnerSwapBtn, plannedMeal);
                    break;
            }
        }

        boolean hasMeals = !meals.isEmpty();
        daysWithMeals.put(date, hasMeals);
        updateDayHighlights();
    }

    private void resetMealCardToAdd(ConstraintLayout card, TextView nameView, TextView timeView, ShapeableImageView imageView, MaterialButton swapBtn, String addText) {
        nameView.setText(addText);
        timeView.setText("Tap to search meals");
        imageView.setImageResource(R.drawable.chef);
        swapBtn.setText("Add");
        swapBtn.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.add));
    }

    private void populateMealCard(TextView nameView, TextView timeView, ShapeableImageView imageView, MaterialButton swapBtn, PlannedMeal plannedMeal) {
        nameView.setText(plannedMeal.getMeal().getStrMeal());
        timeView.setText("30 min");

        Glide.with(requireContext()).load(plannedMeal.getMeal().getStrMealThumb()).placeholder(R.drawable.ic_launcher_background).error(R.drawable.chef).into(imageView);

        swapBtn.setText("Swap");
        swapBtn.setIcon(ContextCompat.getDrawable(requireContext(), R.drawable.rotate_right));
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showSuccess(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void showEmptyState() {

    }

}