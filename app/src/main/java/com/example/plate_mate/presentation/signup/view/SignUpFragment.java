package com.example.plate_mate.presentation.signup.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.plate_mate.MainActivity;
import com.example.plate_mate.R;
import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.repo.AuthRepoImp;
import com.example.plate_mate.presentation.signup.presenter.SignUpPresenterImp;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpFragment extends Fragment implements SignUpView {

    private SignUpPresenterImp presenter;
    private TextInputLayout tilName, tilEmail, tilPassword;
    private ProgressBar progressBar;
    private View btnSignUp, btnBack, btnContinueAsGuest;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new SignUpPresenterImp(new AuthRepoImp(new AuthRemoteDataSource(), AuthPrefManager.getInstance(requireContext())), this);

        initViews(view);

        setupListeners();
    }

    private void initViews(View view) {
        tilName = view.findViewById(R.id.nameLayout);
        tilEmail = view.findViewById(R.id.emailLayout);
        tilPassword = view.findViewById(R.id.passwordLayout);
        progressBar = view.findViewById(R.id.progressBar);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        btnBack = view.findViewById(R.id.btnBack);
        btnContinueAsGuest = view.findViewById(R.id.btnContinueAsGuest);
    }

    private void setupListeners() {
        btnSignUp.setOnClickListener(v -> onSignUpClicked());

        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        btnContinueAsGuest.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });
    }

    private void onSignUpClicked() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);

        String name = tilName.getEditText() != null ? tilName.getEditText().getText().toString().trim() : "";
        String email = tilEmail.getEditText() != null ? tilEmail.getEditText().getText().toString().trim() : "";
        String password = tilPassword.getEditText() != null ? tilPassword.getEditText().getText().toString().trim() : "";

        if (!validateInputs(name, email, password)) {
            return;
        }
        presenter.register(name, email, password);
    }

    private boolean validateInputs(String name, String email, String password) {
        boolean isValid = true;

        if (name.isEmpty()) {
            tilName.setError("Name is required");
            isValid = false;
        } else if (name.length() < 2) {
            tilName.setError("Name must be at least 2 characters");
            isValid = false;
        }

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Invalid email format");
            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("Password must be at least 6 characters");
            isValid = false;
        }

        return isValid;
    }

    @Override
    public void onRegistrationSuccess() {
        Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onRegistrationError(String message) {
        String userMessage = parseErrorMessage(message);
        Toast.makeText(requireContext(), userMessage, Toast.LENGTH_LONG).show();
    }

    @Override
    public void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (btnSignUp != null) {
            btnSignUp.setEnabled(!isLoading);
        }
        if (btnBack != null) {
            btnBack.setEnabled(!isLoading);
        }
        if (btnContinueAsGuest != null) {
            btnContinueAsGuest.setEnabled(!isLoading);
        }
    }

    private String parseErrorMessage(String errorMessage) {
        if (errorMessage == null) {
            return "Registration failed. Please try again.";
        }

        if (errorMessage.contains("email address is already in use")) {
            return "This email is already registered. Please login instead.";
        } else if (errorMessage.contains("email address is badly formatted")) {
            return "Invalid email format. Please check your email.";
        } else if (errorMessage.contains("password is invalid")) {
            return "Password must be at least 6 characters.";
        } else if (errorMessage.contains("network error")) {
            return "Network error. Please check your connection.";
        } else {
            return errorMessage;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.detachView();
        }
    }
}