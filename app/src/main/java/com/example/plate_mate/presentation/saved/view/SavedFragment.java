package com.example.plate_mate.presentation.saved.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.presentation.home.view.MealAdapter;
import com.example.plate_mate.presentation.saved.presenter.SavedPresenter;
import com.example.plate_mate.presentation.saved.presenter.SavedPresenterImp;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SavedFragment extends Fragment implements SavedView {

    private RecyclerView rvFavorites;
    private View layoutEmptyState;
    private MealAdapter adapter;
    private SavedPresenter presenter;

    public SavedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        rvFavorites = view.findViewById(R.id.rvFavorites);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);

        // Setup RecyclerView
        rvFavorites.setLayoutManager(new LinearLayoutManager(getContext()));

        // Initialize adapter with click listeners
        adapter = new MealAdapter(
                new ArrayList<>(),
                this::navigateToMealDetails,
                this::onFavoriteClick
        );
        rvFavorites.setAdapter(adapter);

        // Initialize presenter
        presenter = new SavedPresenterImp(requireContext(), this);

        // Load favorites
        presenter.loadFavorites();
    }

    @Override
    public void showFavorites(List<Meal> favorites) {
        if (adapter != null) {
            adapter.updateMeals(favorites);
        }
    }

    @Override
    public void updateFavorites(Set<String> favoriteMealIds) {
        if (adapter != null) {
            adapter.updateFavorites(favoriteMealIds);
        }
    }

    @Override
    public void showEmptyState() {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.VISIBLE);
        }
        if (rvFavorites != null) {
            rvFavorites.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideEmptyState() {
        if (layoutEmptyState != null) {
            layoutEmptyState.setVisibility(View.GONE);
        }
        if (rvFavorites != null) {
            rvFavorites.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToMealDetails(Meal meal) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("selected_meal", meal);
        Navigation.findNavController(requireView()).navigate(R.id.nav_details, bundle);
    }

    private void onFavoriteClick(Meal meal, boolean currentlyFavorite) {
        // Toggle favorite (which in this screen will always remove it)
        presenter.toggleFavorite(meal);

        // The presenter's observable will automatically update the list
        // No need to manually update adapter here
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.dispose();
        }
    }
}