// presentation/ui/fragment/AddPhotoFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddPhotoFragment extends Fragment {

    private ImageView imageViewPreview;
    private Uri photoUri;
    private PhotoViewModel photoViewModel;
    public static final String ROOT_ALBUM_ID = "1";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_photo, container, false);
    }
    /**
     * Launcher for Camera Intent.
     */
    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoUri != null) {
                    displayImage(photoUri);
                    savePhotoToDatabase(photoUri);
                } else {
                    Toast.makeText(requireContext(), "Camera operation failed", Toast.LENGTH_SHORT).show();
                }
            });
    /**
     * Launcher for Gallery Intent.
     */
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    displayImage(uri);
                    Log.d("AddPhotoFragment", "onActivityResult: " + uri);
                    savePhotoToDatabase(uri);
                } else {
                    Toast.makeText(requireContext(), "No image selected", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageViewPreview = view.findViewById(R.id.imageViewPreview);
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        view.findViewById(R.id.btnOpenCamera).setOnClickListener(v -> openCamera());
        view.findViewById(R.id.btnOpenGallery).setOnClickListener(v -> openGallery());
    }

    /**
     * Open Camera and Capture Image.
     */
    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            photoUri = createImageUri();
            Log.d("AddPhotoFragment", "openCamera: " + photoUri);
            if (photoUri != null) {
                cameraLauncher.launch(photoUri);
            } else {
                Toast.makeText(requireContext(), "Failed to create image URI", Toast.LENGTH_SHORT).show();
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }    /**
     * Permission Launcher for Camera and Storage.
     */
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera(); // Retry the camera intent
                } else {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    /**
     * Open Gallery to Select Image.
     */
    private void openGallery() {
        galleryLauncher.launch("image/*");
    }

    /**
     * Create an image URI in MediaStore.
     */
    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Captured_Image_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "Captured via Camera");
        return requireContext().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * Display selected/captured image in ImageView.
     */
    private void displayImage(Uri uri) {
        Glide.with(this).load(uri).into(imageViewPreview);
    }

    /**
     * Save photo URI to database.
     */
    private void savePhotoToDatabase(Uri uri) {
        Photo photo = new Photo(
                String.valueOf(System.currentTimeMillis()),
                uri.toString()
        );
        if (photo.getFilePath() != null) {
            photoViewModel.addPhoto(photo);
            Toast.makeText(requireContext(), "Photo added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to save photo", Toast.LENGTH_SHORT).show();
        }
    }


}
