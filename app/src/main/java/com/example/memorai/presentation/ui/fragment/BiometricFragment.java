package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricPrompt;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import com.example.memorai.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;

public class BiometricFragment extends BottomSheetDialogFragment {

    public interface OnBiometricVerifiedListener {
        void onBiometricVerified();
    }

    private OnBiometricVerifiedListener onBiometricVerifiedListener;
    private BiometricPrompt biometricPrompt;
    private boolean isAuthenticationInProgress = false;
    private String userId;
    private String mode;

    public void setOnBiometricVerifiedListener(OnBiometricVerifiedListener listener) {
        this.onBiometricVerifiedListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            mode = getArguments().getString("mode", "verify");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        // Inflate layout cho BiometricFragment (đảm bảo R.layout.fragment_biometric tồn
        // tại)
        return inflater.inflate(R.layout.fragment_biometric, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        startBiometricAuthWithDelay();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog != null) {
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) bottomSheet.getLayoutParams();
                params.width = LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(params);
                BottomSheetBehavior<?> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setSkipCollapsed(true);
                behavior.setHideable(false);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        cancelBiometricAuth();
    }

    private void startBiometricAuthWithDelay() {
        if (!isBiometricAvailable()) {
            Toast.makeText(requireContext(), getString(R.string.biometric_not_supported), Toast.LENGTH_SHORT).show();
            return;
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (getView() != null && !isAuthenticationInProgress) {
                initializeBiometricPrompt();
                showBiometricPrompt();
            }
        }, 500);
    }

    private void initializeBiometricPrompt() {
        biometricPrompt = new BiometricPrompt(this,
                ContextCompat.getMainExecutor(requireContext()),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        isAuthenticationInProgress = false;
                        if (onBiometricVerifiedListener != null) {
                            onBiometricVerifiedListener.onBiometricVerified();
                        }
                        dismiss();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        isAuthenticationInProgress = false;
                        Toast.makeText(requireContext(), getString(R.string.biometric_authentication_failed),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        isAuthenticationInProgress = false;
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                            Toast.makeText(requireContext(),
                                   R.string.biometric_authentication_error,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showBiometricPrompt() {
        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.biometric_prompt_title))
                .setSubtitle(getString(R.string.biometric_prompt_subtitle))
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
            androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager
                    .from(requireContext());
            return biometricManager.canAuthenticate(
                    androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG |
                            androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;
        } catch (Exception e) {
            return false;
        }
    }
}
