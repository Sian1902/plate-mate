package com.example.plate_mate.presentation.home.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Area;
import com.example.plate_mate.data.meal.model.Category;
import com.example.plate_mate.data.meal.model.Ingredient;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.presentation.home.presenter.HomePresenter;
import com.example.plate_mate.presentation.home.presenter.HomePresenterImp;
import com.google.android.material.chip.Chip;

import java.util.List;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class HomeFragment extends Fragment implements HomeView {

    private RecyclerView rvMeals;
    private MealAdapter adapter;
    private ImageView ivHeroImage;
    private TextView tvHeroTitle;

    private Chip chipCountry;
    private Chip chipCategory;
    private Chip chipIngredients;

    private HomePresenter homePresenter;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // Store filter options
    private List<Area> areas;
    private List<Category> categories;
    private List<Ingredient> ingredients;

    // Current selections
    private String selectedCountry = null;
    private String selectedCategory = null;
    private String selectedIngredient = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivHeroImage = view.findViewById(R.id.ivHeroMealImage);
        tvHeroTitle = view.findViewById(R.id.tvHeroMealTitle);
        rvMeals = view.findViewById(R.id.rvMeals);
        rvMeals.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize chips
        chipCountry = view.findViewById(R.id.chipCountry);
        chipCategory = view.findViewById(R.id.chipCategory);
        chipIngredients = view.findViewById(R.id.chipIngredients);

        setupChipListeners();

        homePresenter = new HomePresenterImp(getContext().getApplicationContext(), this);
        homePresenter.loadHomeData();
    }

    private void setupChipListeners() {
        chipCountry.setOnClickListener(v -> showCountryDropdown());
        chipCategory.setOnClickListener(v -> showCategoryDropdown());
        chipIngredients.setOnClickListener(v -> showIngredientsDropdown());
    }

    private void showCountryDropdown() {
        if (areas == null || areas.isEmpty()) {
            Toast.makeText(getContext(), "Loading countries...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] countryNames = areas.stream()
                .map(Area::getStrArea)
                .toArray(String[]::new);

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Country")
                .setItems(countryNames, (dialog, which) -> {
                    selectedCountry = countryNames[which];
                    chipCountry.setText(selectedCountry);
                    applyFilter();
                })
                .setNegativeButton("Clear", (dialog, which) -> {
                    selectedCountry = null;
                    chipCountry.setText("Country");
                    applyFilter();
                })
                .show();
    }

    private void showCategoryDropdown() {
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(getContext(), "Loading categories...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] categoryNames = categories.stream()
                .map(Category::getStrCategory)
                .toArray(String[]::new);

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Category")
                .setItems(categoryNames, (dialog, which) -> {
                    selectedCategory = categoryNames[which];
                    chipCategory.setText(selectedCategory);
                    applyFilter();
                })
                .setNegativeButton("Clear", (dialog, which) -> {
                    selectedCategory = null;
                    chipCategory.setText("Category");
                    applyFilter();
                })
                .show();
    }

    private void showIngredientsDropdown() {
        if (ingredients == null || ingredients.isEmpty()) {
            Toast.makeText(getContext(), "Loading ingredients...", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] ingredientNames = ingredients.stream()
                .map(Ingredient::getStrIngredient)
                .toArray(String[]::new);

        new AlertDialog.Builder(requireContext())
                .setTitle("Select Ingredient")
                .setItems(ingredientNames, (dialog, which) -> {
                    selectedIngredient = ingredientNames[which];
                    chipIngredients.setText(selectedIngredient);
                    applyFilter();
                })
                .setNegativeButton("Clear", (dialog, which) -> {
                    selectedIngredient = null;
                    chipIngredients.setText("Ingredients");
                    applyFilter();
                })
                .show();
    }

    private void applyFilter() {
        // Request filtered data from presenter
        homePresenter.filterMeals(selectedCategory, selectedCountry, selectedIngredient);
    }

    @Override
    public void setupUi(List<Meal> mealList, Meal heroMeal) {
        if (mealList != null && !mealList.isEmpty()) {
            tvHeroTitle.setText(heroMeal.getStrMeal());
            Glide.with(this).load(heroMeal.getStrMealThumb()).into(ivHeroImage);

            adapter = new MealAdapter(mealList, meal -> {
                // Handle meal click
            });
            rvMeals.setAdapter(adapter);
        }
    }

    @Override
    public void setFilterOptions(List<Category> categories, List<Area> areas, List<Ingredient> ingredients) {
        this.categories = categories;
        this.areas = areas;
        this.ingredients = ingredients;
    }

    @Override
    public void updateMealList(List<Meal> meals) {
        if (adapter != null && meals != null) {
            adapter.updateMeals(meals);
        }
    }

    @Override
    public void showLoading() {
        // Show loading indicator if you have one
    }

    @Override
    public void hideLoading() {
        // Hide loading indicator
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}