package com.example.plate_mate.presentation.home.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment implements HomeView {
    private static final String TAG = "HomeFragment";

    private RecyclerView rvMeals;
    private MealAdapter adapter;
    private ImageView ivHeroImage;
    private TextView tvHeroTitle;
    private View layoutHeroMeal;
    private HomePresenter homePresenter;

    private List<Category> categories = new ArrayList<>();
    private List<Area> areas = new ArrayList<>();
    private List<Ingredient> ingredients = new ArrayList<>();

    private EditText etSearch;
    private ImageView ivClearSearch;
    private com.google.android.material.chip.Chip chipCountry, chipCategory, chipIngredients;
    private boolean isClearing = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");

        initViews(view);

        homePresenter = new HomePresenterImp(requireContext(), this);

        setupSearch();
        setupChips();

        homePresenter.loadHomeData();
    }

    private void initViews(View view) {
        rvMeals = view.findViewById(R.id.rvMeals);
        ivHeroImage = view.findViewById(R.id.ivHeroMealImage);
        tvHeroTitle = view.findViewById(R.id.tvHeroMealTitle);
        layoutHeroMeal = view.findViewById(R.id.layoutHeroMeal);
        etSearch = view.findViewById(R.id.etSearch);
        ivClearSearch = view.findViewById(R.id.ivClearSearch);
        chipCountry = view.findViewById(R.id.chipCountry);
        chipCategory = view.findViewById(R.id.chipCategory);
        chipIngredients = view.findViewById(R.id.chipIngredients);

        rvMeals.setLayoutManager(new LinearLayoutManager(getContext()));

        view.findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            isClearing = true;
            resetFiltersUI();
            homePresenter.clearAllFilters();
            isClearing = false;
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isClearing) return;
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                homePresenter.searchMeals(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        ivClearSearch.setOnClickListener(v -> etSearch.setText(""));
    }

    private void setupChips() {
        chipCountry.setOnClickListener(v -> showFilterDialog("Country", areas.stream().map(Area::getStrArea).toArray(String[]::new), (name) -> homePresenter.filterMeals(null, name, null), chipCountry));

        chipCategory.setOnClickListener(v -> showFilterDialog("Category", categories.stream().map(Category::getStrCategory).toArray(String[]::new), (name) -> homePresenter.filterMeals(name, null, null), chipCategory));

        chipIngredients.setOnClickListener(v -> showFilterDialog("Ingredient", ingredients.stream().map(Ingredient::getStrIngredient).toArray(String[]::new), (name) -> homePresenter.filterMeals(null, null, name), chipIngredients));
    }

    private void showFilterDialog(String title, String[] items, FilterAction action, com.google.android.material.chip.Chip chip) {
        if (items.length == 0) {
            Toast.makeText(getContext(), "No " + title.toLowerCase() + " data available offline", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext()).setTitle(title).setItems(items, (d, i) -> {
            String selected = items[i];
            chip.setText(selected);
            chip.setChecked(true);
            action.onFilterSelected(selected);
        }).show();
    }

    private void resetFiltersUI() {
        etSearch.setText("");
        ivClearSearch.setVisibility(View.GONE);
        chipCountry.setText("Country");
        chipCountry.setChecked(false);
        chipCategory.setText("Category");
        chipCategory.setChecked(false);
        chipIngredients.setText("Ingredients");
        chipIngredients.setChecked(false);
    }

    @Override
    public void setupUi(List<Meal> meals, Meal hero) {
        Log.d(TAG, "setupUi called with " + meals.size() + " meals, hero: " + (hero != null ? hero.getStrMeal() : "null"));

        if (hero != null) {
            tvHeroTitle.setText(hero.getStrMeal());
            Glide.with(this).load(hero.getStrMealThumb()).into(ivHeroImage);
            layoutHeroMeal.setOnClickListener(v -> navigate(hero));
            layoutHeroMeal.setVisibility(View.VISIBLE);
        } else {
            layoutHeroMeal.setVisibility(View.GONE);
            Log.d(TAG, "No hero meal to display");
        }

        adapter = new MealAdapter(new ArrayList<>(meals), this::navigate, this::onFavoriteClick);
        rvMeals.setAdapter(adapter);

        Log.d(TAG, "Adapter initialized with " + meals.size() + " meals");
    }

    private void navigate(Meal meal) {
        Bundle b = new Bundle();
        b.putSerializable("selected_meal", meal);
        Navigation.findNavController(requireView()).navigate(R.id.nav_details, b);
    }

    private void onFavoriteClick(Meal meal, boolean currentlyFavorite) {
        homePresenter.toggleFavorite(meal);
        if (adapter != null) {
            adapter.toggleFavorite(meal.getIdMeal());
        }
    }

    @Override
    public void updateMealList(List<Meal> m) {
        Log.d(TAG, "updateMealList called with " + m.size() + " meals");
        if (adapter != null) {
            adapter.updateMeals(new ArrayList<>(m));
        }
    }

    @Override
    public void updateFavorites(Set<String> favoriteMealIds) {
        Log.d(TAG, "updateFavorites called with " + favoriteMealIds.size() + " favorites");
        if (adapter != null) {
            adapter.updateFavorites(favoriteMealIds);
        }
    }

    @Override
    public void setFilterOptions(List<Category> c, List<Area> a, List<Ingredient> i) {
        this.categories = c != null ? c : new ArrayList<>();
        this.areas = a != null ? a : new ArrayList<>();
        this.ingredients = i != null ? i : new ArrayList<>();

        Log.d(TAG, "Filter options set - Categories: " + categories.size() + ", Areas: " + areas.size() + ", Ingredients: " + ingredients.size());
    }

    @Override
    public void showError(String message) {
        Log.e(TAG, "Error: " + message);
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (homePresenter instanceof HomePresenterImp) {
            ((HomePresenterImp) homePresenter).dispose();
        }
    }

    private interface FilterAction {
        void onFilterSelected(String name);
    }
}