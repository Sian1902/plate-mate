package com.example.plate_mate.presentation.signin.view;

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
import androidx.navigation.Navigation;
import com.example.plate_mate.MainActivity;
import com.example.plate_mate.R;
import com.example.plate_mate.data.auth.datastore.local.AuthPrefManager;
import com.example.plate_mate.data.auth.datastore.remote.AuthRemoteDataSource;
import com.example.plate_mate.data.auth.repo.AuthRepoImp;
import com.example.plate_mate.presentation.signin.presenter.SignInPresenter;
import com.example.plate_mate.presentation.signin.presenter.SignInPresenterImp;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;

public class SignInFragment extends Fragment implements SignInView {

    private SignInPresenter presenter;
    private TextInputLayout tilEmail, tilPassword;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_SIGN_IN = 100;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        presenter = new SignInPresenterImp(
                new AuthRepoImp(new AuthRemoteDataSource(), AuthPrefManager.getInstance(getContext())),
                this
        );

        tilEmail = view.findViewById(R.id.emailLayout);
        tilPassword = view.findViewById(R.id.passwordLayout);

        view.findViewById(R.id.btnSignIn).setOnClickListener(v ->
                presenter.login(tilEmail.getEditText().getText().toString().trim(), tilPassword.getEditText().getText().toString().trim())
        );

        view.findViewById(R.id.btnGoogleSignIn).setOnClickListener(v -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        view.findViewById(R.id.btnContinueAsGuest).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });



        view.findViewById(R.id.tvSignUpLink).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_signin_to_signup));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                presenter.loginWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                onLoginError("Google login failed: " + e.getMessage());
            }
        }
    }

    @Override
    public void onLoginSuccess() {
        startActivity(new Intent(getActivity(), MainActivity.class));
        requireActivity().finish();
    }

    @Override
    public void onLoginError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setLoading(boolean isLoading) {
        // Toggle your Lottie animation or ProgressBar here
        View loader = getView().findViewById(R.id.lottieProgressBar);
        if (loader != null) loader.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
}