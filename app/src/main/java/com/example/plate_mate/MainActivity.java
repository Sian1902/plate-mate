package com.example.plate_mate;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.meal.datasource.remote.FirebaseSyncDataSource;
import com.example.plate_mate.data.meal.model.FirebaseFavorite;
import com.example.plate_mate.data.meal.model.FirebasePlannedMeal;
import com.example.plate_mate.data.meal.model.Meal;
import com.example.plate_mate.data.meal.model.MealResponse;
import com.example.plate_mate.data.meal.model.MealType;
import com.example.plate_mate.data.meal.model.PlannedMeal;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.presentation.mealdetails.view.MealDetailsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements MealDetailsFragment.NavVisibilityCallback {
    private NavController navController;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    private AuthRemoteDataSource authRemoteDataSource;
    private FirebaseSyncDataSource firebaseSyncDataSource;
    private MealRepoImp mealRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authRemoteDataSource = new AuthRemoteDataSource();
        firebaseSyncDataSource = new FirebaseSyncDataSource();
        mealRepository = MealRepoImp.getInstance(this);

        checkUserAndDownloadData();

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            bottomNav = findViewById(R.id.bottomNavigation);
            toolbar = findViewById(R.id.mainToolbar);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }
    }

    private void checkUserAndDownloadData() {
        FirebaseUser user = authRemoteDataSource.getCurrentUser();
        if (user != null) {
            // User is signed in, download their data from Firebase
            downloadUserData(user.getUid());
        }
    }

    private void downloadUserData(String userId) {
        Observable.zip(
                        downloadFavorites(userId).toObservable(),
                        downloadPlannedMeals(userId).toObservable(),
                        (favCount, plannedCount) -> new int[]{favCount, plannedCount}
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        counts -> {
                            // Data downloaded successfully
                            // You could show a notification or log this
                        },
                        error -> {
                            // Handle download error
                            // You might want to retry or just ignore for now
                        }
                );
    }

    private io.reactivex.rxjava3.core.Single<Integer> downloadFavorites(String userId) {
        return firebaseSyncDataSource.fetchUserFavorites(userId)
                .flatMap(firebaseFavorites -> {
                    if (firebaseFavorites.isEmpty()) {
                        return io.reactivex.rxjava3.core.Single.just(0);
                    }

                    return Observable.fromIterable(firebaseFavorites)
                            .flatMapSingle(favorite ->
                                    mealRepository.getMealById(favorite.getMealId())
                                            .flatMap(mealResponse -> {
                                                if (mealResponse.getMeals() != null && !mealResponse.getMeals().isEmpty()) {
                                                    Meal meal = mealResponse.getMeals().get(0);
                                                    return mealRepository.insertFavorite(meal)
                                                            .toSingleDefault(1)
                                                            .onErrorReturnItem(0);
                                                }
                                                return io.reactivex.rxjava3.core.Single.just(0);
                                            })
                            )
                            .reduce(0, Integer::sum);
                });
    }

    private io.reactivex.rxjava3.core.Single<Integer> downloadPlannedMeals(String userId) {
        return firebaseSyncDataSource.fetchUserPlannedMeals(userId)
                .flatMap(firebasePlannedMeals -> {
                    if (firebasePlannedMeals.isEmpty()) {
                        return io.reactivex.rxjava3.core.Single.just(0);
                    }

                    return Observable.fromIterable(firebasePlannedMeals)
                            .flatMapSingle(plannedMeal ->
                                    mealRepository.getMealById(plannedMeal.getMealId())
                                            .flatMap(mealResponse -> {
                                                if (mealResponse.getMeals() != null && !mealResponse.getMeals().isEmpty()) {
                                                    Meal meal = mealResponse.getMeals().get(0);

                                                    PlannedMeal localPlannedMeal = new PlannedMeal(
                                                            plannedMeal.getDate(),
                                                            MealType.valueOf(plannedMeal.getMealType()),
                                                            plannedMeal.getMealId(),
                                                            meal,
                                                            System.currentTimeMillis()
                                                    );

                                                    return mealRepository.insertPlannedMeal(localPlannedMeal)
                                                            .toSingleDefault(1)
                                                            .onErrorReturnItem(0);
                                                }
                                                return io.reactivex.rxjava3.core.Single.just(0);
                                            })
                            )
                            .reduce(0, Integer::sum);
                });
    }

    @Override
    public void setNavigationVisibility(boolean isVisible) {
        runOnUiThread(() -> {
            if (bottomNav != null) {
                bottomNav.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }

            if (toolbar != null) {
                toolbar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        return Navigation.findNavController(this, R.id.nav_host_fragment).navigateUp();
    }
}