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
import com.example.plate_mate.presentation.profile.presenter.ProfilePresenter;
import com.example.plate_mate.presentation.profile.contract.ProfileContract;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment implements ProfileContract.View {

    private TextInputEditText nameEditText;
    private TextInputEditText emailEditText;
    private SwitchMaterial darkModeSwitch;
    private LinearLayout resetPasswordLayout;
    private LinearLayout logoutLayout;
    private ProgressBar progressBar;

    private ProfilePresenter presenter;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AuthRemoteDataSource remoteDataSource = new AuthRemoteDataSource();
        AuthPrefManager prefManager = AuthPrefManager.getInstance(requireContext());
        AuthRepoImp authRepo = new AuthRepoImp(remoteDataSource, prefManager);

        presenter = new ProfilePresenter(authRepo, remoteDataSource);
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
            presenter.onResetPasswordClicked();
        });

        logoutLayout.setOnClickListener(v -> {
            presenter.onLogoutClicked();
        });
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
    }

    @Override
    public void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
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