package com.example.plate_mate.presentation.signup.presenter;

public interface SignUpPresenter {
    void register(String name, String email, String password);

    void detachView();
}