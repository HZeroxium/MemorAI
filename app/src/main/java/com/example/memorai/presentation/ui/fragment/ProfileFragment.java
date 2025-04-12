package com.example.memorai.presentation.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentProfileBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoSection;
import com.example.memorai.presentation.ui.adapter.PhotoSectionAdapter;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.example.memorai.presentation.viewmodel.UserViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private UserViewModel userViewModel;
    private PhotoViewModel photoViewModel;
    private AlbumViewModel albumViewModel;
    private PhotoSectionAdapter adapter;
    private boolean isSelectionMode = false;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);

        // Add back button functionality
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(requireView()).popBackStack());

        // Add click listeners for stats sections
        setupNavigationListeners();

        setupObservers();
        adapter = new PhotoSectionAdapter();
        adapter.setOnPhotoClickListener((sharedView, photo) -> {
            if (!isSelectionMode) {
                // Pass photoId & photoUrl for the detail
                Bundle args = new Bundle();
                args.putString("photo_id", photo.getId());
                args.putByteArray("photo_url", convertBitmapToByteArray(photo.getBitmap()));
                Navigation.findNavController(view).navigate(R.id.photoDetailFragment, args);
                Toast.makeText(requireContext(), "Path: " + photo.getFilePath(), Toast.LENGTH_SHORT).show();
            }
        });

        binding.recyclerViewPhotos.setAdapter(adapter);
        applyLayoutManager();
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), this::handlePhotoList);
    }

    private void setupNavigationListeners() {
        // Navigate to Photos fragment
        binding.layoutPhotosStats
                .setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.photoListFragment));

        // Navigate to Albums fragment
        binding.layoutAlbumsStats
                .setOnClickListener(v -> Navigation.findNavController(requireView()).navigate(R.id.albumListFragment));

        // Navigate to Private album
        binding.layoutPrivatesStats.setOnClickListener(v -> {
            // Find the private album and navigate to it
            photoViewModel.getPrivateAlbumId(albumId -> {
                if (albumId != null) {
                    Bundle args = new Bundle();
                    args.putString("album_id", albumId);
                    photoViewModel.clearAlbumPhoto();
                    albumViewModel.clearAlbum();
                    Navigation.findNavController(requireView()).navigate(R.id.albumDetailFragment, args);
                } else {
                    Toast.makeText(requireContext(), "Private album not found", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    private void applyLayoutManager() {
        // We'll default to 3 columns for a Google Photos style (or 2 if you prefer)
        final int spanCount = 3;

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // If item is a section header, take the full row
                int viewType = adapter.getItemViewType(position);
                if (viewType == PhotoSectionAdapter.TYPE_HEADER) {
                    return spanCount; // header spans all columns
                } else {
                    return 1; // photo items occupy 1 column each
                }
            }
        });
        binding.recyclerViewPhotos.setLayoutManager(layoutManager);
    }

    private void handlePhotoList(List<Photo> photos) {
        List<PhotoSection> sections = groupPhotos(photos);
        adapter.setData(sections);
    }

    private List<PhotoSection> groupPhotos(List<Photo> photos) {
        List<PhotoSection> result = new ArrayList<>();
        result.add(new PhotoSection(getString(R.string.all_photos), photos));
        return result;
    }

    private void setupObservers() {
        if (user != null) {
            updateUI(user);
        }

        userViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        binding.username.setText(user.getDisplayName());

        // Load avatar using Glide
        if (!TextUtils.isEmpty(user.getPhotoUrl().toString())) {
            Glide.with(requireContext())
                    .load(user.getPhotoUrl())
                    .circleCrop()
                    .into(binding.avatar);
        } else {
            binding.avatar.setImageResource(R.drawable.placeholder_image);
        }

        // Update stats
        binding.photosCountText.setText(String.valueOf(photoViewModel.getPhotoCount()));
        binding.albumsCountText.setText(String.valueOf(albumViewModel.getAlbumCount()));
        binding.albumPrivate.setText(String.valueOf(photoViewModel.getPrivatePhotoCount()));
        // Setup buttons
        binding.btnAddMedia.setOnClickListener(v -> showAddMediaDialog());
        binding.btnEditProfile.setOnClickListener(v -> openEditProfile(user));
    }


    private void showAddMediaDialog() {
        // Implement your add media dialog
    }

    private void openEditProfile(FirebaseUser user) {
        // Implement edit profile functionality
=======

        // We could update private count here if we have a method to get that count
        // binding.privatesCountText.setText(String.valueOf(photoViewModel.getPrivatePhotoCount()));
    }
}