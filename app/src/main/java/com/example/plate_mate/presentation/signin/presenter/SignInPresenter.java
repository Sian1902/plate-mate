package com.example.plate_mate.presentation.signin.presenter;

public interface SignInPresenter {
    void login(String email, String password);

    void loginWithGoogle(String idToken);
}
