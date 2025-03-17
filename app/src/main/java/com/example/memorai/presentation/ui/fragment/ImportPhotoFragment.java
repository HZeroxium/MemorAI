// presentation/ui/fragment/ImportPhotoFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.databinding.FragmentImportPhotoBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ImportPhotoFragment extends Fragment {

    private final List<Photo> importedPhotos = new ArrayList<>();
    private FragmentImportPhotoBinding binding;
    private PhotoViewModel photoViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;
    private final ActivityResultLauncher<String> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                            requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (SecurityException e) {
                            uri = copyImageToAppStorage(uri);
                        }

                        Photo photo = new Photo(UUID.randomUUID().toString(), uri.toString(), new ArrayList<>(), System.currentTimeMillis(), System.currentTimeMillis());
                        importedPhotos.add(photo);
                    }
                    selectedPhotoAdapter.submitList(new ArrayList<>(importedPhotos));
                } else {
                    Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show();
                }
            });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentImportPhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        setupRecyclerView();
        binding.buttonSelectFromGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.buttonConfirmImport.setOnClickListener(v -> confirmImport());
        binding.toolbarImportPhoto.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewImportedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewImportedPhotos.setAdapter(selectedPhotoAdapter);

        // Implement remove functionality
        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            importedPhotos.remove(photo); // Remove from list
            selectedPhotoAdapter.submitList(new ArrayList<>(importedPhotos)); // Refresh UI
        });
    }


    private void confirmImport() {
        if (importedPhotos.isEmpty()) {
            Toast.makeText(requireContext(), "No photos imported", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Confirm Import")
                .setMessage("Add " + importedPhotos.size() + " photo(s) to database?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    for (Photo p : importedPhotos) {
                        photoViewModel.addPhoto(p);
                    }
                    Toast.makeText(requireContext(), "Photos imported", Toast.LENGTH_SHORT).show();
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

    private Uri copyImageToAppStorage(Uri sourceUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
            if (inputStream == null) return sourceUri;

            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File newFile = new File(storageDir, "IMG_" + System.currentTimeMillis() + ".jpg");

            OutputStream outputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return FileProvider.getUriForFile(requireContext(), "com.example.memorai.fileprovider", newFile);
        } catch (IOException e) {
            e.printStackTrace();
            return sourceUri;
        }
    }

}
