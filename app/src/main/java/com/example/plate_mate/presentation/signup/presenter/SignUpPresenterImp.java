package com.example.plate_mate.presentation.signup.presenter;

import com.example.plate_mate.data.auth.repo.AuthRepo;
import com.example.plate_mate.presentation.signup.contract.SignUpContract;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SignUpPresenterImp implements SignUpContract.Presenter {
    private final AuthRepo repo;
    private SignUpContract.View view;
    private final CompositeDisposable disposables = new CompositeDisposable();

    public SignUpPresenterImp(AuthRepo repo, SignUpContract.View view) {
        this.repo = repo;
        this.view = view;
    }

    @Override
    public void register(String name, String email, String password) {
        // Validate inputs
        if (name == null || name.trim().isEmpty()) {
            if (view != null) {
                view.onRegistrationError("Name is required");
            }
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            if (view != null) {
                view.onRegistrationError("Email is required");
            }
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            if (view != null) {
                view.onRegistrationError("Password is required");
            }
            return;
        }

        if (password.length() < 6) {
            if (view != null) {
                view.onRegistrationError("Password must be at least 6 characters");
            }
            return;
        }

        if (view != null) {
            view.setLoading(true);
        }

        disposables.add(
                repo.register(name.trim(), email.trim(), password.trim())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> {
                                    if (view != null) {
                                        view.setLoading(false);
                                        view.onRegistrationSuccess();
                                    }
                                },
                                error -> {
                                    if (view != null) {
                                        view.setLoading(false);
                                        view.onRegistrationError(error.getMessage());
                                    }
                                }
                        )
        );
    }

    @Override
    public void detachView() {
        this.view = null;
        disposables.clear();
    }
}