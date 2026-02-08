package com.example.plate_mate.presentation.signin.view;


public interface SignInView {
    void onLoginSuccess();
    void onLoginError(String message);
    void setLoading(boolean isLoading);
}