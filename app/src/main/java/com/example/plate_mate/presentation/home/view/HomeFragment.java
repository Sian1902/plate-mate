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

import java.util.List;

public class HomeFragment extends Fragment implements HomeView {

    private RecyclerView rvMeals;
    private MealAdapter adapter;
    private ImageView ivHeroImage;
    private TextView tvHeroTitle;
    private View layoutHeroMeal;
    private HomePresenter homePresenter;

    // Filter data storage
    private List<Category> categories;
    private List<Area> areas;
    private List<Ingredient> ingredients;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        rvMeals = view.findViewById(R.id.rvMeals);
        ivHeroImage = view.findViewById(R.id.ivHeroMealImage);
        tvHeroTitle = view.findViewById(R.id.tvHeroMealTitle);
        layoutHeroMeal = view.findViewById(R.id.layoutHeroMeal);

        // Setup RecyclerView
        rvMeals.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize Presenter with Context and View
        homePresenter = new HomePresenterImp(requireContext(), this);

        // Trigger data loading
        homePresenter.loadHomeData();
    }

    @Override
    public void setupUi(List<Meal> mealList, Meal heroMeal) {
        // Setup Hero Meal (Meal of the Day)
        if (heroMeal != null) {
            tvHeroTitle.setText(heroMeal.getStrMeal());
            Glide.with(this).load(heroMeal.getStrMealThumb()).into(ivHeroImage);

            // Navigate to details when clicking the Hero Card
            layoutHeroMeal.setOnClickListener(v -> navigateToDetails(heroMeal));
        }

        // Setup Horizontal/Vertical List
        if (mealList != null) {
            adapter = new MealAdapter(mealList, this::navigateToDetails);
            rvMeals.setAdapter(adapter);
        }
    }

    private void navigateToDetails(Meal meal) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("selected_meal", meal);

        // Ensure "nav_details" or your specific detail ID is defined in nav_graph.xml
        Navigation.findNavController(requireView())
                .navigate(R.id.nav_details, bundle);
    }

    @Override
    public void updateMealList(List<Meal> meals) {
        if (adapter != null && meals != null) {
            adapter.updateMeals(meals); //
        }
    }

    @Override
    public void setFilterOptions(List<Category> categories, List<Area> areas, List<Ingredient> ingredients) {
        this.categories = categories;
        this.areas = areas;
        this.ingredients = ingredients;
    }

    @Override
    public void showLoading() {
        // Implement progress bar visibility here if added to XML
    }

    @Override
    public void hideLoading() {
        // Hide progress bar here
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clean up RxJava disposables in presenter
        if (homePresenter instanceof HomePresenterImp) {
            ((HomePresenterImp) homePresenter).dispose();
        }
    }
}