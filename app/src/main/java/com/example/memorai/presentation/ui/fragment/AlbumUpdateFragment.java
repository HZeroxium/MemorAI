package com.example.memorai.presentation.ui.fragment;

import static com.example.memorai.utils.ImageUtils.convertImageToBase64;

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
import java.util.List;

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

    public AlbumUpdateFragment() {
        super(R.layout.fragment_album_update);
    }

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
        loadAlbumAndPhotos();
    }

    private void setupToolbar(View view) {
        binding.toolbarAlbumUpdate.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewSelectedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewSelectedPhotos.setAdapter(selectedPhotoAdapter);

        // Remove photo from album
        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            albumCreationViewModel.removePhoto(photo);
            showSnackbar(getString(R.string.photo_removed));
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

        // Observe album data
        albumViewModel.getAlbumLiveData().observe(getViewLifecycleOwner(), album -> {
            if (album == null)
                return;
            currentAlbum = album;
            binding.editTextAlbumTitle.setText(album.getName());
        });

        // Observe photos of the album
        albumViewModel.getPhotosLiveData().observe(getViewLifecycleOwner(), photos -> {
            if (photos != null && !photos.isEmpty()) {
                albumCreationViewModel.setPhotos(photos);
            }
        });
    }

    private void validateForm() {
        String title = binding.editTextAlbumTitle.getText().toString().trim();
        List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();

        boolean isValid = !TextUtils.isEmpty(title) && selected != null && !selected.isEmpty();
        binding.buttonUpdateAlbum.setEnabled(isValid);
    }

    private void loadAlbumAndPhotos() {
        if (albumId == null)
            return;

        // Load album details from local (Room)
        albumViewModel.loadAlbumById(albumId);

        // Load photos for this album
        albumViewModel.loadPhotosFromAlbum(albumId);
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
            String description = ""; // Nếu không có description trong UI, để trống

            // Validation
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(requireContext(), R.string.enter_album_title, Toast.LENGTH_SHORT).show();
                return;
            }

            List<Photo> selectedPhotos = albumCreationViewModel.getSelectedPhotos().getValue();
            if (selectedPhotos == null || selectedPhotos.isEmpty()) {
                Toast.makeText(requireContext(), R.string.no_photos_selected, Toast.LENGTH_SHORT).show();
                return;
            }

            // Directly use the first selected photo's Base64 string as the album cover
            String coverPhotoBase64 = selectedPhotos.get(0).getFilePath();

            // Create updated album with new information
            Album updatedAlbum = new Album(
                    albumId,
                    title,
                    description,
                    currentAlbum.getPhotos(), // Keep the old photo list for now, will update later
                    coverPhotoBase64,
                    currentAlbum.getCreatedAt(),
                    System.currentTimeMillis(),
                    currentAlbum.isPrivate() // Maintain the same privacy setting
            );

            // Cập nhật album với danh sách ảnh mới
            albumViewModel.updateAlbumWithPhotos(updatedAlbum, selectedPhotos);

            Toast.makeText(requireContext(), R.string.album_updated, Toast.LENGTH_SHORT).show();

            // Clear selection và quay lại
            albumCreationViewModel.clear();
            Navigation.findNavController(view).popBackStack();
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}