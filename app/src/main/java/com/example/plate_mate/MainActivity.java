package com.example.plate_mate;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.presentation.mealdetails.view.MealDetailsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements MealDetailsFragment.NavVisibilityCallback {
    private NavController navController;
    private BottomNavigationView bottomNav;
    private Toolbar toolbar;
    private AuthPrefManager authPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        authPrefManager = AuthPrefManager.getInstance(this);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            bottomNav = findViewById(R.id.bottomNavigation);
            toolbar = findViewById(R.id.mainToolbar);

            setupBottomNavigation();
            setupNavigationListener();
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (authPrefManager.isGuest()) {
                if (itemId == R.id.nav_saved || itemId == R.id.nav_planner) {
                    showGuestModeSnackbar();
                    return false;
                }
            }

            NavigationUI.onNavDestinationSelected(item, navController);
            return true;
        });
    }

    private void setupNavigationListener() {
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            if (authPrefManager.isGuest()) {
                int destinationId = destination.getId();
                if (destinationId == R.id.nav_saved || destinationId == R.id.nav_planner) {
                    navController.popBackStack(R.id.nav_home, false);
                    showGuestModeSnackbar();
                }
            }
        });
    }

    private void showGuestModeSnackbar() {
        View rootView = findViewById(android.R.id.content);
        Snackbar.make(rootView, "Please sign in to access this feature", Snackbar.LENGTH_LONG)
                .setAction("Sign In", v -> {
                    navController.navigate(R.id.nav_profile);
                })
                .show();
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