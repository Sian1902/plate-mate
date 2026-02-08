package com.example.plate_mate.data.auth.repo;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface AuthRepo {
    Completable login(String email, String password);
    Completable register(String email, String password);
    Completable loginWithGoogle(String idToken);
    Completable logout();

    // Local Session Checks
    boolean isUserLoggedIn();
    boolean isGuestMode();
    String getCurrentUserId();
}