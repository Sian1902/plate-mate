package com.example.plate_mate.presentation.signin.contract;

public interface SignInContract {

    interface Presenter {
        void login(String email, String password);

        void loginWithGoogle(String idToken);
    }

    interface View {
        void onLoginSuccess();
        void onLoginError(String message);
        void setLoading(boolean isLoading);
    }
}
