package com.example.plate_mate.presentation.signup.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.plate_mate.MainActivity;
import com.example.plate_mate.R;
import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.repo.AuthRepoImp;
import com.example.plate_mate.presentation.signup.presenter.SignUpPresenterImp;
import com.google.android.material.textfield.TextInputLayout;

public class SignUpFragment extends Fragment implements SignUpView {

    private SignUpPresenterImp presenter;
    private TextInputLayout tilEmail, tilPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new SignUpPresenterImp(
                new AuthRepoImp(new AuthRemoteDataSource(), AuthPrefManager.getInstance(getContext())),
                this
        );

        tilEmail = view.findViewById(R.id.emailLayout);
        tilPassword = view.findViewById(R.id.passwordLayout);

        view.findViewById(R.id.btnSignUp).setOnClickListener(v ->
                presenter.register(tilEmail.getEditText().getText().toString().trim(), tilPassword.getEditText().getText().toString().trim())
        );

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                getActivity().onBackPressed());
    }

    @Override
    public void onRegistrationSuccess() {
        startActivity(new Intent(getActivity(), MainActivity.class));
        requireActivity().finish();
    }

    @Override
    public void onRegistrationError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setLoading(boolean isLoading) {
    }
}