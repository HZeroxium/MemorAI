// presentation/ui/fragment/AlbumUpdateFragment.java
package com.example.memorai.presentation.ui.fragment;

import static com.example.memorai.utils.ImageUtils.convertImageToBase64;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumUpdateBinding;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumCreationViewModel;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment to update an existing album.
 */
@AndroidEntryPoint
public class AlbumUpdateFragment extends Fragment {

    private FragmentAlbumUpdateBinding binding;
    private AlbumViewModel albumViewModel;
    private PhotoViewModel photoViewModel;
    private AlbumCreationViewModel albumCreationViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;
    private String albumId;

    private FirebaseUser user;

    private Album currentAlbum;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumUpdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        albumCreationViewModel = new ViewModelProvider(requireActivity()).get(AlbumCreationViewModel.class);
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        if (getArguments() != null) {
            albumId = getArguments().getString("album_id", "");
        }

        setupToolbar(view);
        setupRecyclerView();
        setupObservers();
        setupButtons(view);
        user = FirebaseAuth.getInstance().getCurrentUser();
        loadAlbumAndPhotos(user.getUid());
    }

    private void setupToolbar(View view) {
        binding.toolbarAlbumUpdate.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewSelectedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewSelectedPhotos.setAdapter(selectedPhotoAdapter);

        // Remove photo from album
        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            albumCreationViewModel.removePhoto(photo);
            showSnackbar("Photo removed from album");
        });
    }

    private void setupObservers() {
        // Observe selected photos
        albumCreationViewModel.getSelectedPhotos().observe(getViewLifecycleOwner(), photos -> {
            selectedPhotoAdapter.submitList(photos);
            validateForm();
        });

        // Validate form whenever text changes
        binding.editTextAlbumTitle.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }
            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void validateForm() {
        String title = binding.editTextAlbumTitle.getText().toString().trim();
        List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();

        boolean isValid = !TextUtils.isEmpty(title) && selected != null && !selected.isEmpty();
        binding.buttonUpdateAlbum.setEnabled(isValid);
    }

    private void loadAlbumAndPhotos(String userId) {
        if (userId == null || albumId == null) return;

        // Load album details
        albumViewModel.loadAlbumById(albumId);
        albumViewModel.getAlbumLiveData().observe(getViewLifecycleOwner(), album -> {
            if (album == null) return;
            currentAlbum = album;
            binding.editTextAlbumTitle.setText(album.getName());

            // Load photos for this album
            if (album.getPhotos() != null && !album.getPhotos().isEmpty()) {
                albumViewModel.loadPhotosFromAlbum(userId, albumId);
            }
        });

        // Observe photos
        albumViewModel.getPhotosLiveData().observe(getViewLifecycleOwner(), photos -> {
            if (photos != null && !photos.isEmpty()) {
                albumCreationViewModel.setPhotos(photos);
            }
        });
    }

    private void setupButtons(View view) {
        binding.buttonSelectPhotos.setOnClickListener(v -> {
            // Pass the current album ID to the photo selection fragment
            Bundle args = new Bundle();
            args.putString("album_id", albumId);
            Navigation.findNavController(view).navigate(R.id.albumAddPhotosFragment, args);
        });

        binding.buttonUpdateAlbum.setOnClickListener(v -> {
            String title = binding.editTextAlbumTitle.getText().toString().trim();
            String description = "";

            // Validation
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(requireContext(), "Enter a valid title", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(requireContext(), "No photos selected for album!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Extract photo IDs
            List<String> photoIds = new ArrayList<>();
            for (Photo photo : selected) {
                photoIds.add(photo.getId());
            }

            Uri contentUri = Uri.parse(selected.get(0).getFilePath());

            String base64 = convertImageToBase64(requireContext(), contentUri);

            // Update album
            user = FirebaseAuth.getInstance().getCurrentUser();
            String userId = user.getUid();
            if (userId != null) {

                Map<String, Object> updatedAlbum = new HashMap<>();
                updatedAlbum.put("id", albumId);
                updatedAlbum.put("title", title);
                updatedAlbum.put("description", "");
                updatedAlbum.put("photos", photoIds);
                updatedAlbum.put("coverPhotoUrl", base64);
                updatedAlbum.put("createdAt", System.currentTimeMillis());
                updatedAlbum.put("updatedAt", System.currentTimeMillis());
                // Update in Firestore
                albumViewModel.updateAlbumWithPhotos(userId, albumId, photoIds);
                Toast.makeText(requireContext(), "Album updated!", Toast.LENGTH_SHORT).show();

                // Clear selection and go back
                albumCreationViewModel.clear();
                Navigation.findNavController(view).popBackStack();
            }
        });
    }

    // Helper method to get Bitmap from Photo (implement according to your storage)
    private Bitmap getBitmapFromPhoto(Photo photo) {
        // Implement your logic to get Bitmap from photo
        // This could be loading from file, decoding from base64, etc.
        return null; // placeholder
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadAlbumAndPhotos() {
        // 1) Get album info from VM
        albumViewModel.getAlbumById(albumId).observe(getViewLifecycleOwner(), album -> {
            if (album == null) return;
            currentAlbum = album;
            binding.editTextAlbumTitle.setText(album.getName());
        });

    }

}
