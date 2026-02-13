package com.example.plate_mate;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements MealDetailsFragment.NavVisibilityCallback {
    private NavController navController;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    private AuthRemoteDataSource authRemoteDataSource;
    private FirebaseSyncDataSource firebaseSyncDataSource;
    private MealRepoImp mealRepository;

    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private AlertDialog noConnectionDialog;
    private boolean isConnected = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authRemoteDataSource = new AuthRemoteDataSource();
        firebaseSyncDataSource = new FirebaseSyncDataSource();
        mealRepository = MealRepoImp.getInstance(this);
        setupNetworkMonitoring();

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

    private void setupNetworkMonitoring() {
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                runOnUiThread(() -> {
                    isConnected = true;
                    dismissNoConnectionDialog();
                });
            }

            @Override
            public void onLost(@NonNull Network network) {
                super.onLost(network);
                runOnUiThread(() -> {
                    isConnected = false;
                    showNoConnectionDialog();
                });
            }

            @Override
            public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities);
                boolean hasInternet = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                        networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

                runOnUiThread(() -> {
                    if (hasInternet && !isConnected) {
                        isConnected = true;
                        dismissNoConnectionDialog();
                    } else if (!hasInternet && isConnected) {
                        isConnected = false;
                        showNoConnectionDialog();
                    }
                });
            }
        };

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        checkInitialConnectivity();
    }

    private void checkInitialConnectivity() {
        Network network = connectivityManager.getActiveNetwork();
        if (network == null) {
            isConnected = false;
            showNoConnectionDialog();
            return;
        }

        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
        if (capabilities == null) {
            isConnected = false;
            showNoConnectionDialog();
            return;
        }

        isConnected = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);

        if (!isConnected) {
            showNoConnectionDialog();
        }
    }

    private void showNoConnectionDialog() {
        if (noConnectionDialog != null && noConnectionDialog.isShowing()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setCancelable(false)
                .setNegativeButton("Close", (dialog, which) -> {
                    dialog.dismiss();
                });

        noConnectionDialog = builder.create();
        noConnectionDialog.show();
    }

    private void dismissNoConnectionDialog() {
        if (noConnectionDialog != null && noConnectionDialog.isShowing()) {
            noConnectionDialog.dismiss();
        }
    }

    private void checkUserAndDownloadData() {
        FirebaseUser user = authRemoteDataSource.getCurrentUser();
        if (user != null) {
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
                            int favs = counts[0];
                            int planned = counts[1];
                            Log.d("MainActivity", "Sync Complete: " + favs + " favorites and " + planned + " planned meals downloaded.");
                        },
                        error -> {
                            Log.e("MainActivity", "Sync failed: " + error.getMessage());
                        }
                );
    }

    private Single<Integer> downloadFavorites(String userId) {
        return firebaseSyncDataSource.fetchUserFavorites(userId)
                .flatMap(firebaseFavorites -> {
                    if (firebaseFavorites.isEmpty()) {
                        return Single.just(0);
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
                                                return Single.just(0);
                                            })
                            )
                            .reduce(0, Integer::sum);
                });
    }

    private Single<Integer> downloadPlannedMeals(String userId) {
        return firebaseSyncDataSource.fetchUserPlannedMeals(userId)
                .flatMap(firebasePlannedMeals -> {
                    if (firebasePlannedMeals.isEmpty()) {
                        return Single.just(0);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
        dismissNoConnectionDialog();
    }
}