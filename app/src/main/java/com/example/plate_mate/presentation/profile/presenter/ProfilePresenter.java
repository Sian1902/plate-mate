package com.example.plate_mate.presentation.profile.presenter;

import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.model.User;
import com.example.plate_mate.data.auth.repo.AuthRepo;
import com.example.plate_mate.presentation.profile.contract.ProfileContract;
import com.google.firebase.auth.FirebaseUser;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ProfilePresenter implements ProfileContract.Presenter {

    private ProfileContract.View view;
    private final AuthRepo authRepo;
    private final AuthRemoteDataSource remoteDataSource;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public ProfilePresenter(AuthRepo authRepo, AuthRemoteDataSource remoteDataSource) {
        this.authRepo = authRepo;
        this.remoteDataSource = remoteDataSource;
    }

    @Override
    public void attachView(ProfileContract.View view) {
        this.view = view;
        loadUserProfile();
        boolean isDarkModeEnabled = authRepo.isDarkModeEnabled();
        if (view != null) {
            view.updateDarkModeSwitch(isDarkModeEnabled);
        }
    }

    @Override
    public void detachView() {
        this.view = null;
        disposables.clear();
    }

    @Override
    public void loadUserProfile() {
        if (view == null) return;

        view.showLoading(true);

        try {
            FirebaseUser firebaseUser = remoteDataSource.getCurrentUser();

            if (firebaseUser != null) {
                User user = new User(
                        firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "",
                        firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "User",
                        firebaseUser.getUid()
                );
                view.showUserData(user);
            } else {
                view.showError("No user logged in");
            }

            view.showLoading(false);
        } catch (Exception e) {
            view.showLoading(false);
            view.showError("Failed to load profile: " + e.getMessage());
        }
    }

    @Override
    public void onDarkModeToggled(boolean isEnabled) {
        authRepo.setDarkMode(isEnabled);
        if (view != null) {
            view.showSuccess(isEnabled ? "Dark mode enabled" : "Dark mode disabled");
        }
    }

    @Override
    public void onResetPasswordClicked() {
        if (view == null) return;

        FirebaseUser user = remoteDataSource.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            view.showLoading(true);

            com.google.firebase.auth.FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(user.getEmail())
                    .addOnSuccessListener(aVoid -> {
                        if (view != null) {
                            view.showLoading(false);
                            view.showSuccess("Password reset email sent to " + user.getEmail());
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (view != null) {
                            view.showLoading(false);
                            view.showError("Failed to send reset email: " + e.getMessage());
                        }
                    });
        } else {
            view.showError("No email associated with this account");
        }
    }

    @Override
    public void onLogoutClicked() {
        if (view == null) return;

        view.showLoading(true);

        disposables.add(
                authRepo.logout()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showSuccess("Logged out successfully");
                                        view.navigateToLogin();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.showLoading(false);
                                        view.showError("Logout failed: " + error.getMessage());
                                    }
                                }
                        )
        );
    }
}