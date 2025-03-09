// presentation/ui/fragment/AlbumUpdateFragment.java
package com.example.memorai.presentation.ui.fragment;

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

        loadAlbumAndPhotos();
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

    private void setupButtons(View view) {
        binding.buttonSelectPhotos.setOnClickListener(v -> {
//            Navigation.findNavController(view).navigate(R.id.albumAddPhotosFragment);
        });

        binding.buttonUpdateAlbum.setOnClickListener(v -> {
            String title = binding.editTextAlbumTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(requireContext(), "Enter a valid title", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(requireContext(), "No photos selected for album!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Actually do the update
            currentAlbum = new Album(albumId, title, "", selected.get(0).getFilePath(),
                    System.currentTimeMillis(), System.currentTimeMillis());
            albumViewModel.updateAlbumWithPhotos(currentAlbum, selected);
            Toast.makeText(requireContext(), "Album updated!", Toast.LENGTH_SHORT).show();

            // Clear creationViewModel and go back
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

    private void loadAlbumAndPhotos() {
        // 1) Get album info from VM
        albumViewModel.getAlbumById(albumId).observe(getViewLifecycleOwner(), album -> {
            if (album == null) return;
            currentAlbum = album;
            binding.editTextAlbumTitle.setText(album.getName());
        });

    }

}