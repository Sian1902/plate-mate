package com.example.plate_mate.presentation.signup.view;

public interface SignUpView {
    void onRegistrationSuccess();

    void onRegistrationError(String message);

    void setLoading(boolean isLoading);
}