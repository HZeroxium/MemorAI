// presentation/ui/fragment/TakePhotoFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.databinding.FragmentTakePhotoBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TakePhotoFragment extends Fragment {

    private final List<Photo> capturedPhotos = new ArrayList<>();
    private FragmentTakePhotoBinding binding;
    private PhotoViewModel photoViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;
    private Uri currentPhotoUri;

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && currentPhotoUri != null) {
                    Photo photo = new Photo(UUID.randomUUID().toString(), currentPhotoUri.toString(), new ArrayList<>(), System.currentTimeMillis(), System.currentTimeMillis());
                    capturedPhotos.add(photo);
                    selectedPhotoAdapter.submitList(new ArrayList<>(capturedPhotos));
                } else {
                    Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTakePhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        setupRecyclerView();
        binding.buttonCapture.setOnClickListener(v -> openCamera());
        binding.buttonConfirm.setOnClickListener(v -> confirmPhotos());
        binding.toolbarTakePhoto.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewCapturedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewCapturedPhotos.setAdapter(selectedPhotoAdapter);

        // Implement remove functionality
        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            capturedPhotos.remove(photo); // Remove from list
            selectedPhotoAdapter.submitList(new ArrayList<>(capturedPhotos)); // Refresh UI
        });
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            currentPhotoUri = createImageUri();
            if (currentPhotoUri != null) {
                cameraLauncher.launch(currentPhotoUri);
            } else {
                Toast.makeText(requireContext(), "Unable to create image URI", Toast.LENGTH_SHORT).show();
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "IMG_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "Photo captured via CameraX");
        return requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private void confirmPhotos() {
        if (capturedPhotos.isEmpty()) {
            Toast.makeText(requireContext(), "No photos captured", Toast.LENGTH_SHORT).show();
            return;
        }
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Add " + capturedPhotos.size() + " photo(s) to database?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    for (Photo p : capturedPhotos) {
                        photoViewModel.addPhoto(p);
                    }
                    Toast.makeText(requireContext(), "Photos added", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}
