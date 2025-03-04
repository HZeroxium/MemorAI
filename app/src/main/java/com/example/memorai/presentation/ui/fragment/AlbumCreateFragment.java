// presentation/ui/fragment/AlbumCreateFragment.java
// presentation/ui/fragment/AlbumCreateFragment.java
package com.example.memorai.presentation.ui.fragment;

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

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumCreateBinding;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumCreationViewModel;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumCreateFragment extends Fragment {

    private FragmentAlbumCreateBinding binding;
    private AlbumViewModel albumViewModel;
    private AlbumCreationViewModel albumCreationViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        albumCreationViewModel = new ViewModelProvider(requireActivity()).get(AlbumCreationViewModel.class);

        binding.toolbarAlbumCreate.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        setupRecyclerView();
        observeSelectedPhotos();
        setupButtons(view);
        setupToolbar(view);
    }

    private void setupToolbar(View view) {
        binding.toolbarAlbumCreate.setNavigationOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewSelectedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewSelectedPhotos.setAdapter(selectedPhotoAdapter);

        // If user wants to remove a photo from the selection
        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            albumCreationViewModel.removePhoto(photo);
        });
    }

    private void observeSelectedPhotos() {
        albumCreationViewModel.getSelectedPhotos().observe(getViewLifecycleOwner(), photos -> {
            selectedPhotoAdapter.submitList(photos);
            binding.buttonAddAlbum.setEnabled(!photos.isEmpty());
        });
    }

    private void setupButtons(View view) {
        binding.buttonSelectPhotos.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.albumAddPhotosFragment)
        );

        binding.buttonAddAlbum.setOnClickListener(v -> {
            String title = binding.editTextAlbumTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(requireContext(), "Please enter an album title", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(requireContext(), "No photos selected for album!", Toast.LENGTH_SHORT).show();
                return;
            }

            // First photo is cover
            String coverUrl = selected.get(0).getFilePath();

            Album album = new Album(
                    UUID.randomUUID().toString(),
                    title,
                    "", // or user-provided description
                    coverUrl,
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
            );

            // For now, just do a background thread to insert or update
            new Thread(() -> {
                albumViewModel.createAlbumWithPhotos(album, selected);
                // Possibly also add each Photo with albumId if you store that relation
            }).start();

            Toast.makeText(requireContext(),
                    "Album created with " + selected.size() + " photos!",
                    Toast.LENGTH_SHORT).show();

            albumCreationViewModel.clear();
            Navigation.findNavController(view).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

