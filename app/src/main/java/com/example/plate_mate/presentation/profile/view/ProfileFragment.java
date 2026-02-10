package com.example.plate_mate.presentation.profile.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.plate_mate.AuthActivity;
import com.example.plate_mate.R;
import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.model.User;
import com.example.plate_mate.data.auth.repo.AuthRepoImp;
import com.example.plate_mate.data.meal.datasource.remote.FirebaseSyncDataSource;
import com.example.plate_mate.data.meal.repository.MealRepoImp;
import com.example.plate_mate.presentation.profile.contract.ProfileContract;
import com.example.plate_mate.presentation.profile.presenter.ProfilePresenter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment implements ProfileContract.View {

    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private SwitchMaterial darkModeSwitch;
    private LinearLayout resetPasswordLayout;
    private LinearLayout logoutLayout;
    private MaterialButton uploadDataButton;
    private MaterialCardView syncSection;
    private ProgressBar progressBar;

    private ProfilePresenter presenter;
    private AuthPrefManager authPrefManager;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authPrefManager = AuthPrefManager.getInstance(requireContext());

        AuthRemoteDataSource remoteDataSource = new AuthRemoteDataSource();
        AuthPrefManager prefManager = AuthPrefManager.getInstance(requireContext());
        AuthRepoImp authRepo = new AuthRepoImp(remoteDataSource, prefManager);
        MealRepoImp mealRepo = MealRepoImp.getInstance(requireContext());
        FirebaseSyncDataSource firebaseSyncDataSource = new FirebaseSyncDataSource();

        presenter = new ProfilePresenter(authRepo, remoteDataSource, mealRepo, firebaseSyncDataSource);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        setupListeners();
        updateUIForGuestMode();

        presenter.attachView(this);

        return view;
    }

    private void initViews(View view) {
        nameEditText = view.findViewById(R.id.nameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch);
        resetPasswordLayout = view.findViewById(R.id.resetPasswordLayout);
        logoutLayout = view.findViewById(R.id.logoutLayout);
        uploadDataButton = view.findViewById(R.id.uploadDataButton);
        syncSection = view.findViewById(R.id.syncSection);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                presenter.onDarkModeToggled(isChecked);
                applyDarkMode(isChecked);
            }
        });

        resetPasswordLayout.setOnClickListener(v -> {
            if (authPrefManager.isGuest()) {
                showGuestModeSnackbar("sign in to reset your password");
                return;
            }
            presenter.onResetPasswordClicked();
        });

        logoutLayout.setOnClickListener(v -> presenter.onLogoutClicked());

        uploadDataButton.setOnClickListener(v -> {
            if (authPrefManager.isGuest()) {
                showGuestModeSnackbar("sign in to backup your data");
                return;
            }
            showUploadConfirmationDialog();
        });
    }

    private void updateUIForGuestMode() {
        if (authPrefManager.isGuest()) {
            // Hide sync section for guest users
            if (syncSection != null) {
                syncSection.setVisibility(View.GONE);
            }
        } else {
            if (syncSection != null) {
                syncSection.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showGuestModeSnackbar(String action) {
        Snackbar.make(requireView(), "Please " + action, Snackbar.LENGTH_LONG)
                .setAction("Sign In", v -> {
                    Intent intent = new Intent(requireContext(), AuthActivity.class);
                    startActivity(intent);
                })
                .show();
    }

    private void showUploadConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Backup to Cloud")
                .setMessage("This will upload all your local favorites and planned meals to Firebase cloud storage. Continue?")
                .setPositiveButton("Upload", (dialog, which) -> presenter.onUploadDataClicked())
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void showLogoutWarningDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Logout Warning")
                .setMessage("Logging out will delete all your local data (favorites and planned meals).\n\n" +
                        "Make sure you've backed up your data to the cloud before logging out.\n\n" +
                        "What would you like to do?")
                .setPositiveButton("Backup & Logout", (dialog, which) -> {
                    presenter.onUploadDataClicked();
                    showBackupThenLogoutMessage();
                })
                .setNegativeButton("Logout Without Backup", (dialog, which) -> {
                    showFinalLogoutConfirmation();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    private void showBackupThenLogoutMessage() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Backup Complete")
                .setMessage("Your data has been backed up to the cloud.\n\nYou can now logout safely. Would you like to logout now?")
                .setPositiveButton("Logout Now", (dialog, which) -> presenter.onLogoutConfirmed())
                .setNegativeButton("Stay Logged In", null)
                .show();
    }

    private void showFinalLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("⚠️ Final Confirmation")
                .setMessage("Are you absolutely sure you want to logout without backing up?\n\n" +
                        "ALL your favorites and planned meals will be PERMANENTLY DELETED from this device.")
                .setPositiveButton("Yes, Delete Everything", (dialog, which) -> presenter.onLogoutConfirmed())
                .setNegativeButton("Cancel", null)
                .setCancelable(false)
                .show();
    }

    @Override
    public void showUserData(User user) {
        if (nameEditText != null && emailEditText != null) {
            nameEditText.setText(user.getName());
            emailEditText.setText(user.getEmail());
            emailEditText.setEnabled(false);
        }
    }

    @Override
    public void showLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (uploadDataButton != null && !authPrefManager.isGuest()) {
            uploadDataButton.setEnabled(!isLoading);
        }
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
    public void navigateToLogin() {
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void updateDarkModeSwitch(boolean isEnabled) {
        if (darkModeSwitch != null) {
            darkModeSwitch.setChecked(isEnabled);
        }
    }

    @Override
    public void showUploadProgress(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showUploadComplete(int favoritesCount, int plannedMealsCount) {
        String message = String.format("Backup complete!\n✓ %d favorites\n✓ %d planned meals uploaded to cloud",
                favoritesCount, plannedMealsCount);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Backup Successful")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    private void applyDarkMode(boolean isEnabled) {
        if (isEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        presenter.detachView();
    }
}