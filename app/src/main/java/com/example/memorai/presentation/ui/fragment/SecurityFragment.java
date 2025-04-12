package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.example.memorai.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import android.widget.FrameLayout;
import android.view.ViewGroup.LayoutParams;

public class SecurityFragment extends BottomSheetDialogFragment {
    public interface PinVerificationListener {
        void onPinVerified();
    }

    private PinVerificationListener pinVerificationListener;
    private String albumId;
    private String userId;
    // Cờ để đảm bảo dismiss chỉ được gọi một lần
    private boolean isDismissed = false;
    private boolean isVerified = false;
    public void setPinVerificationListener(PinVerificationListener listener) {
        this.pinVerificationListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            albumId = getArguments().getString("albumId");
            userId = getArguments().getString("userId");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View rootView = inflater.inflate(R.layout.fragment_security, container, false);
        Button pinButton = rootView.findViewById(R.id.btnPin);
        Button biometricButton = rootView.findViewById(R.id.btnBiometric);

        pinButton.setOnClickListener(v -> openPinFragment());
        biometricButton.setOnClickListener(v -> openBiometricFragment());

        return rootView;
    }

    public boolean isVerified() {
        return isVerified;
    }

    private void openPinFragment(){
        PinFragment pinFragment = new PinFragment();
        Bundle args = new Bundle();
        args.putString("mode", "verify");
        args.putString("userId", userId);
        pinFragment.setArguments(args);
        pinFragment.setOnPinVerifiedListener(() -> {
            isVerified = true;
            if(pinVerificationListener != null){
                pinVerificationListener.onPinVerified();
            }
            if(!isDismissed){
                isDismissed = true;
                dismiss();
            }
        });
        pinFragment.show(getParentFragmentManager(), "PinFragment");
    }

    private void openBiometricFragment(){
        // Mở BiometricFragment dưới dạng dialog
        BiometricFragment biometricFragment = new BiometricFragment();
        Bundle args = new Bundle();
        args.putString("mode", "verify");
        args.putString("userId", userId);
        biometricFragment.setArguments(args);
        biometricFragment.setOnBiometricVerifiedListener(() -> {
            isVerified = true;
            if(pinVerificationListener != null){
                pinVerificationListener.onPinVerified();
            }
            if(!isDismissed){
                isDismissed = true;
                dismiss();
            }
        });
        biometricFragment.show(getParentFragmentManager(), "BiometricFragment");
    }

    @Override
    public void onStart(){
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if(dialog != null){
            FrameLayout bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if(bottomSheet != null){
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
}
