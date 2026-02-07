package com.example.plate_mate.presentation.signup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.plate_mate.R;

public class SignUpFragment extends Fragment {

    public SignUpFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Find the "Already have an account? Sign In" link
        TextView tvLoginLink = view.findViewById(R.id.tvLoginLink);
        ImageView ivBack = view.findViewById(R.id.btnBack);



        tvLoginLink.setOnClickListener(v -> {
            // Navigate back to the Sign In screen
            Navigation.findNavController(v).navigateUp();
        });
        ivBack.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
    }
}