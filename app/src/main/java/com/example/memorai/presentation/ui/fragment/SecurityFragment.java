package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.memorai.R;

public class SecurityFragment extends Fragment {

    public SecurityFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_security, container, false);

        Button pinButton = rootView.findViewById(R.id.btnPin);
        Button biometricButton = rootView.findViewById(R.id.btnBiometric);
        Button securityQuestionButton = rootView.findViewById(R.id.btnSecurityQuestion);

        pinButton.setOnClickListener(v -> openPinFragment());
        biometricButton.setOnClickListener(v -> openBiometricFragment());
        securityQuestionButton.setOnClickListener(v -> openSecurityQuestionFragment());

        return rootView;
    }

    private void openPinFragment() {
        // Mở fragment xác thực PIN
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new PinFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openBiometricFragment() {
        // Mở fragment xác thực vân tay hoặc Face ID
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new BiometricFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void openSecurityQuestionFragment() {
        // Mở fragment câu hỏi bảo mật
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.nav_host_fragment, new SecurityQuestionFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
