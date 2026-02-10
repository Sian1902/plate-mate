package com.example.plate_mate.presentation.profile;

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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment implements ProfileContract.View {

    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private SwitchMaterial darkModeSwitch;
    private LinearLayout resetPasswordLayout;
    private LinearLayout logoutLayout;
    private MaterialButton uploadDataButton;
    private MaterialButton downloadDataButton;
    private MaterialButton fullSyncButton;
    private ProgressBar progressBar;

    private ProfilePresenter presenter;

    public ProfileFragment() {
        // Required empty public constructor
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        downloadDataButton = view.findViewById(R.id.downloadDataButton);
        fullSyncButton = view.findViewById(R.id.fullSyncButton);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        darkModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                presenter.onDarkModeToggled(isChecked);
                applyDarkMode(isChecked);
            }
        });

        resetPasswordLayout.setOnClickListener(v -> presenter.onResetPasswordClicked());
        logoutLayout.setOnClickListener(v -> presenter.onLogoutClicked());

        uploadDataButton.setOnClickListener(v -> showUploadConfirmationDialog());
        downloadDataButton.setOnClickListener(v -> showDownloadConfirmationDialog());
        fullSyncButton.setOnClickListener(v -> showFullSyncConfirmationDialog());
    }

    private void showUploadConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Upload to Firebase")
                .setMessage("This will upload all your local favorites and planned meals to Firebase. Continue?")
                .setPositiveButton("Upload", (dialog, which) -> presenter.onUploadDataClicked())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDownloadConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Download from Firebase")
                .setMessage("This will download your favorites and planned meals from Firebase and merge with local data. Continue?")
                .setPositiveButton("Download", (dialog, which) -> presenter.onDownloadDataClicked())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showFullSyncConfirmationDialog() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Full Sync")
                .setMessage("This will:\n1. Upload local data to Firebase\n2. Download Firebase data to device\n\nAll data will be synced. Continue?")
                .setPositiveButton("Sync", (dialog, which) -> presenter.onFullSyncClicked())
                .setNegativeButton("Cancel", null)
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
        if (uploadDataButton != null) uploadDataButton.setEnabled(!isLoading);
        if (downloadDataButton != null) downloadDataButton.setEnabled(!isLoading);
        if (fullSyncButton != null) fullSyncButton.setEnabled(!isLoading);
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
    public void showSyncProgress(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSyncComplete(int favoritesCount, int plannedMealsCount) {
        String message = String.format("Download complete!\n✓ %d favorites\n✓ %d planned meals",
                favoritesCount, plannedMealsCount);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Download Successful")
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void showUploadComplete(int favoritesCount, int plannedMealsCount) {
        String message = String.format("Upload complete!\n✓ %d favorites\n✓ %d planned meals",
                favoritesCount, plannedMealsCount);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Upload Successful")
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