package com.example.memorai.presentation.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentPinBinding;
import com.example.memorai.domain.model.User;
import com.example.memorai.presentation.ui.activity.MainActivity;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class PinFragment extends BottomSheetDialogFragment {
    private FragmentPinBinding binding;
    private User user;
    private static final String CORRECT_PIN = "123456";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPinBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            user = getArguments().getParcelable("user");
        }

        binding.btnConfirm.setOnClickListener(v -> {
            String enteredPin = binding.pinInput.getText().toString();
            if (enteredPin.length() == 6) {
                savePinToFirebase(enteredPin);
            } else {
                Toast.makeText(requireContext(), R.string.pin_length_error, Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnCancel.setOnClickListener(v -> dismiss());
    }

    private void savePinToFirebase(String pin) {
        if (user == null) {
            Toast.makeText(requireContext(), R.string.user_not_found, Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(user.getId());

        userRef.child("pin").setValue(pin)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), R.string.pin_saved, Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), R.string.error_saving_pin, Toast.LENGTH_SHORT).show());
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.putExtra("user", user);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
