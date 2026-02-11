package com.example.plate_mate.presentation.planner.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plate_mate.R;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.repository.MealRepository;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MealSearchDialog extends Dialog {

    private EditText searchEditText;
    private ImageButton clearSearchButton;
    private ProgressBar progressBar;
    private TextView emptySearchText;
    private RecyclerView searchResultsRecycler;
    private MaterialButton cancelButton;

    private MealSearchAdapter adapter;
    private final MealRepository repository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    private final Long selectedDate;
    private final MealType selectedMealType;
    private final OnMealAddedListener listener;

    public interface OnMealAddedListener {
        void onMealAdded();
    }

    public MealSearchDialog(@NonNull Context context, MealRepository repository,
                            Long selectedDate, MealType selectedMealType,
                            OnMealAddedListener listener) {
        super(context);
        this.repository = repository;
        this.selectedDate = selectedDate;
        this.selectedMealType = selectedMealType;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_search_meal);

        initViews();
        setupRecyclerView();
        setupListeners();
    }

    private void initViews() {
        searchEditText = findViewById(R.id.search_edit_text);
        clearSearchButton = findViewById(R.id.clear_search_button);
        progressBar = findViewById(R.id.search_progress);
        emptySearchText = findViewById(R.id.empty_search_text);
        searchResultsRecycler = findViewById(R.id.search_results_recycler);
        cancelButton = findViewById(R.id.cancel_button);
    }

    private void setupRecyclerView() {
        adapter = new MealSearchAdapter(meal -> {
            addPlannedMeal(meal);
        });

        searchResultsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecycler.setAdapter(adapter);
    }

    private void setupListeners() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    clearSearchButton.setVisibility(View.VISIBLE);
                } else {
                    clearSearchButton.setVisibility(View.GONE);
                }

                if (s.length() >= 2) {
                    searchMeals(s.toString());
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            clearSearchButton.setVisibility(View.GONE);
            showEmptyState();
        });

        cancelButton.setOnClickListener(v -> dismiss());
    }

    private void searchMeals(String query) {
        showLoading();

        disposables.add(
                repository.SearchMealsByName(query)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                mealResponse -> {
                                    hideLoading();
                                    if (mealResponse.getMeals() != null && !mealResponse.getMeals().isEmpty()) {
                                        showResults(mealResponse.getMeals());
                                    } else {
                                        showNoResults();
                                    }
                                },
                                error -> {
                                    hideLoading();
                                    showError();
                                }
                        )
        );
    }

    private void addPlannedMeal(Meal meal) {
        showLoading();

        com.example.plate_mate.data.meal.model.PlannedMeal plannedMeal =
                new com.example.plate_mate.data.meal.model.PlannedMeal(
                        selectedDate,
                        selectedMealType,
                        meal.getIdMeal(),
                        meal,
                        System.currentTimeMillis()
                );

        disposables.add(
                repository.insertPlannedMeal(plannedMeal)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    hideLoading();
                                    if (listener != null) {
                                        listener.onMealAdded();
                                    }
                                    dismiss();
                                },
                                error -> {
                                    hideLoading();
                                    // Show error toast or message
                                    showError();
                                }
                        )
        );
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        searchResultsRecycler.setVisibility(View.GONE);
        emptySearchText.setVisibility(View.GONE);
    }

    private void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    private void showResults(List<Meal> meals) {
        searchResultsRecycler.setVisibility(View.VISIBLE);
        emptySearchText.setVisibility(View.GONE);
        adapter.updateMeals(meals);
    }

    private void showNoResults() {
        searchResultsRecycler.setVisibility(View.GONE);
        emptySearchText.setVisibility(View.VISIBLE);
        emptySearchText.setText("No meals found");
    }

    private void showEmptyState() {
        searchResultsRecycler.setVisibility(View.GONE);
        emptySearchText.setVisibility(View.VISIBLE);
        emptySearchText.setText("Type to search for meals...");
        adapter.updateMeals(null);
    }

    private void showError() {
        searchResultsRecycler.setVisibility(View.GONE);
        emptySearchText.setVisibility(View.VISIBLE);
        emptySearchText.setText("Error searching meals. Please try again.");
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposables.clear();
    }
}