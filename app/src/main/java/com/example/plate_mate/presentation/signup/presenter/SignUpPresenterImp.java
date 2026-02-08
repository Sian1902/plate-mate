package com.example.plate_mate.presentation.signup.presenter;

import com.example.plate_mate.data.auth.repo.AuthRepo;
import com.example.plate_mate.presentation.signup.view.SignUpView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SignUpPresenterImp implements SignUpPresenter {
    private final AuthRepo repo;
    private final SignUpView view;

    public SignUpPresenterImp(AuthRepo repo, SignUpView view) {
        this.repo = repo;
        this.view = view;
    }

    @Override
    public void register(String email, String password) {
        repo.register(email, password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::onRegistrationSuccess, error -> view.onRegistrationError(error.getMessage()));
    }
}