package com.example.plate_mate.presentation.signin.presenter;

import com.example.plate_mate.data.auth.repo.AuthRepo;
import com.example.plate_mate.presentation.signin.view.SignInView;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SignInPresenterImp implements SignInPresenter {
    private final AuthRepo repo;
    private final SignInView view;

    public SignInPresenterImp(AuthRepo repo, SignInView view) {
        this.repo = repo;
        this.view = view;
    }

    @Override
    public void login(String email, String password) {
        repo.login(email, password).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(view::onLoginSuccess, error -> view.onLoginError(error.getMessage()));
    }

    @Override
    public void loginWithGoogle(String idToken) {
        repo.loginWithGoogle(idToken).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(view::onLoginSuccess, error -> view.onLoginError(error.getMessage()));
    }
}