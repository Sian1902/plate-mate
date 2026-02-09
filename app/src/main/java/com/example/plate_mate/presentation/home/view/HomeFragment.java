package com.example.plate_mate.presentation.home.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HomeFragment extends Fragment implements HomeView {
    private RecyclerView rvMeals;
    private MealAdapter adapter;
    private ImageView ivHeroImage;
    private TextView tvHeroTitle;
    private View layoutHeroMeal;
    private HomePresenter homePresenter;
    private List<Category> categories;
    private List<Area> areas;
    private List<Ingredient> ingredients;
    private EditText etSearch;
    private ImageView ivClearSearch;
    private Chip chipCountry, chipCategory, chipIngredients;
    private boolean isClearing = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        homePresenter = new HomePresenterImp(requireContext(), this);

        setupSearch();
        setupChips();

        view.findViewById(R.id.tvSeeAll).setOnClickListener(v -> {
            isClearing = true;
            clearUI();
            homePresenter.clearAllFilters();
            isClearing = false;
        });

        homePresenter.loadHomeData();
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isClearing) return;
                ivClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                homePresenter.searchMeals(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
        ivClearSearch.setOnClickListener(v -> etSearch.setText(""));
    }

    private void setupChips() {
        chipCountry.setOnClickListener(v -> {
            if (areas == null) return;
            String[] names = areas.stream().map(Area::getStrArea).toArray(String[]::new);
            new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Country")
                    .setItems(names, (d, i) -> {
                        chipCountry.setText(names[i]); chipCountry.setChecked(true);
                        homePresenter.filterMeals(null, names[i], null);
                    }).show();
        });
        chipCategory.setOnClickListener(v -> {
            if (categories == null) return;
            String[] names = categories.stream().map(Category::getStrCategory).toArray(String[]::new);
            new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Category")
                    .setItems(names, (d, i) -> {
                        chipCategory.setText(names[i]); chipCategory.setChecked(true);
                        homePresenter.filterMeals(names[i], null, null);
                    }).show();
        });
        chipIngredients.setOnClickListener(v -> {
            if (ingredients == null) return;
            String[] names = ingredients.stream().map(Ingredient::getStrIngredient).toArray(String[]::new);
            new androidx.appcompat.app.AlertDialog.Builder(requireContext()).setTitle("Ingredient")
                    .setItems(names, (d, i) -> {
                        chipIngredients.setText(names[i]); chipIngredients.setChecked(true);
                        homePresenter.filterMeals(null, null, names[i]);
                    }).show();
        });
    }

    private void clearUI() {
        etSearch.setText("");
        ivClearSearch.setVisibility(View.GONE);
        chipCountry.setText("Country"); chipCountry.setChecked(false);
        chipCategory.setText("Category"); chipCategory.setChecked(false);
        chipIngredients.setText("Ingredients"); chipIngredients.setChecked(false);
    }

    @Override
    public void setupUi(List<Meal> meals, Meal hero) {
        if (hero != null) {
            tvHeroTitle.setText(hero.getStrMeal());
            Glide.with(this).load(hero.getStrMealThumb()).into(ivHeroImage);
            layoutHeroMeal.setOnClickListener(v -> navigate(hero));
        }
        adapter = new MealAdapter(new ArrayList<>(meals), this::navigate, this::onFavoriteClick);
        rvMeals.setAdapter(adapter);
    }

    private void navigate(Meal meal) {
        Bundle b = new Bundle();
        b.putSerializable("selected_meal", meal);
        Navigation.findNavController(requireView()).navigate(R.id.nav_details, b);
    }

    private void onFavoriteClick(Meal meal, boolean currentlyFavorite) {
        homePresenter.toggleFavorite(meal);
        // Update the adapter immediately for better UX
        if (adapter != null) {
            adapter.toggleFavorite(meal.getIdMeal());
        }
    }

    @Override
    public void updateMealList(List<Meal> m) {
        if (adapter != null) adapter.updateMeals(new ArrayList<>(m));
    }

    @Override
    public void updateFavorites(Set<String> favoriteMealIds) {
        if (adapter != null) {
            adapter.updateFavorites(favoriteMealIds);
        }
    }

    @Override
    public void setFilterOptions(List<Category> c, List<Area> a, List<Ingredient> i) {
        categories = c;
        areas = a;
        ingredients = i;
    }

    @Override
    public void showError(String m) {
        Toast.makeText(getContext(), m, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (homePresenter instanceof HomePresenterImp) ((HomePresenterImp) homePresenter).dispose();
    }
}