package com.example.plate_mate.presentation.signup.contract;

public interface SignUpContract {
    interface Presenter {
        void register(String name, String email, String password);
        void detachView();
    }
    interface View {
        void onRegistrationSuccess();
        void onRegistrationError(String message);
        void setLoading(boolean isLoading);
    }
}
