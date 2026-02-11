package com.example.plate_mate.data.auth.repo;

import io.reactivex.rxjava3.core.Completable;

public interface AuthRepo {
    Completable login(String email, String password);

    Completable register(String name, String email, String password);

    Completable loginWithGoogle(String idToken);

    Completable logout();

    boolean isUserLoggedIn();


    void setDarkMode(boolean isEnabled);

    boolean isDarkModeEnabled();

}