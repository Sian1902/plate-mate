package com.example.plate_mate.data.auth.repo;

import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.rxjava3.core.Completable;

public class AuthRepoImp implements AuthRepo {
    private final AuthRemoteDataSource remoteDataSource;
    private final AuthPrefManager localDataSource;

    public AuthRepoImp(AuthRemoteDataSource remoteDataSource, AuthPrefManager localDataSource) {
        this.remoteDataSource = remoteDataSource;
        this.localDataSource = localDataSource;
    }

    @Override
    public Completable login(String email, String password) {
        return remoteDataSource.signIn(email, password).doOnComplete(() -> saveSessionLocally(false));
    }

    @Override
    public Completable register(String name, String email, String password) {
        return remoteDataSource.signUp(email, password).andThen(remoteDataSource.updateDisplayName(name)).doOnComplete(() -> saveSessionLocally(false));
    }

    @Override
    public Completable loginWithGoogle(String idToken) {
        return remoteDataSource.signInWithGoogle(idToken).doOnComplete(() -> saveSessionLocally(false));
    }

    @Override
    public Completable logout() {
        return Completable.fromAction(() -> {
            remoteDataSource.logout();
            localDataSource.clearSession();
        });
    }


    private void saveSessionLocally(boolean isGuest) {
        FirebaseUser user = remoteDataSource.getCurrentUser();
        if (user != null) {
            localDataSource.saveUserSession(user.getUid(), user.getEmail() != null ? user.getEmail() : "Guest User", isGuest);
        }
    }

    @Override
    public boolean isUserLoggedIn() {
        return localDataSource.isLoggedIn();
    }


    @Override
    public void setDarkMode(boolean isEnabled) {
        localDataSource.setDarkMode(isEnabled);
    }

    @Override
    public boolean isDarkModeEnabled() {
        return localDataSource.isDarkModeEnabled();
    }
}