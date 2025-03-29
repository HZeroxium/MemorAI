package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.example.memorai.R;

public class BiometricFragment extends Fragment {

    private BiometricPrompt biometricPrompt;
    private boolean isAuthenticationInProgress = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Xử lý nút back khi đang xác thực
        requireActivity().getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isAuthenticationInProgress) {
                    cancelBiometricAuth();
                } else {
                    requireActivity().finish();
                }
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_biometric, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        startBiometricAuthWithDelay();
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelBiometricAuth();
    }

    private void startBiometricAuthWithDelay() {
        if (!isBiometricAvailable()) {
            Toast.makeText(requireContext(), "Thiết bị không hỗ trợ xác thực sinh trắc học", Toast.LENGTH_SHORT).show();
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getView() != null && !isAuthenticationInProgress) {
                initializeBiometricPrompt();
                showBiometricPrompt();
            }
        }, 500); // Tăng delay để đảm bảo Fragment hoàn toàn ổn định
    }

    private void initializeBiometricPrompt() {
        biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(requireContext()),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        isAuthenticationInProgress = false;
                        openPrivateAlbumFragment();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        isAuthenticationInProgress = false;
                        Toast.makeText(requireContext(), "Xác thực thất bại", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        isAuthenticationInProgress = false;
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                            Toast.makeText(requireContext(), "Lỗi xác thực: " + errString, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Xác thực vân tay hoặc nhận diện khuôn mặt")
                .setSubtitle("Sử dụng vân tay hoặc khuôn mặt để đăng nhập")
                .setDeviceCredentialAllowed(true)
                .build();

        isAuthenticationInProgress = true;
        biometricPrompt.authenticate(promptInfo);
    }

    private void cancelBiometricAuth() {
        if (isAuthenticationInProgress && biometricPrompt != null) {
            biometricPrompt.cancelAuthentication();
            isAuthenticationInProgress = false;
        }
    }

    private boolean isBiometricAvailable() {
        try {
            androidx.biometric.BiometricManager biometricManager =
                    androidx.biometric.BiometricManager.from(requireContext());
            return biometricManager.canAuthenticate(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG |
                            androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;
        } catch (Exception e) {
            return false;
        }
    }

    private void openPrivateAlbumFragment() {
        if (isAdded() && !requireActivity().isFinishing()) {
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    FragmentTransaction transaction =
                            requireActivity().getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.nav_host_fragment, new PrivateAlbumFragment());
                    transaction.addToBackStack(null);
                    transaction.commitAllowingStateLoss();
                } catch (IllegalStateException e) {
                    // Xử lý exception nếu FragmentManager đang bận
                }
            });
        }
    }
}