// presentation/ui/fragment/AlbumUpdateFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumUpdateFragment extends Fragment {
    private FragmentAlbumUpdateBinding binding;
    private AlbumViewModel albumViewModel;
    private AlbumCreationViewModel albumCreationViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;
    private Album currentAlbum;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumUpdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        albumCreationViewModel = new ViewModelProvider(requireActivity()).get(AlbumCreationViewModel.class);
        String albumId = getArguments() != null ? getArguments().getString("album_id", "") : "";
        albumViewModel.getAlbumById(albumId).observe(getViewLifecycleOwner(), album -> {
            if (album != null) {
                currentAlbum = album;
                binding.editTextAlbumTitle.setText(album.getName());
            }
        });

        setupToolbar(view);
        setupRecyclerView();
        setupObservers();
        setupButtons(view);
    }

    private void setupToolbar(View view) {
        binding.toolbarAlbumUpdate.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());
        binding.toolbarAlbumUpdate.setTitle(R.string.edit_album);
        binding.toolbarAlbumUpdate.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_delete_album) {
                showDeleteConfirmation(view);
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewSelectedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewSelectedPhotos.setAdapter(selectedPhotoAdapter);
        selectedPhotoAdapter.setOnRemoveClickListener(photo -> albumCreationViewModel.removePhoto(photo));
    }

    private void setupObservers() {
        albumCreationViewModel.getSelectedPhotos().observe(getViewLifecycleOwner(), photos -> {
            selectedPhotoAdapter.submitList(photos);
            binding.buttonUpdateAlbum.setEnabled(!photos.isEmpty());
        });
    }

    private void setupButtons(View view) {
        binding.buttonSelectPhotos.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.albumAddPhotosFragment));
        binding.buttonUpdateAlbum.setOnClickListener(v -> {
            String title = binding.editTextAlbumTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(requireContext(), "Please enter an album title", Toast.LENGTH_SHORT).show();
                return;
            }
            List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(requireContext(), "No photos selected!", Toast.LENGTH_SHORT).show();
                return;
            }
            String coverUrl = selected.get(0).getFilePath();
            Album updatedAlbum = new Album(
                    currentAlbum.getId(),
                    title,
                    currentAlbum.getDescription(),
                    coverUrl,
                    currentAlbum.getCreatedAt(),
                    System.currentTimeMillis()
            );
            // Update album record and cross references
            albumViewModel.updateAlbum(updatedAlbum);
            albumViewModel.updateAlbumWithPhotos(updatedAlbum, selected);
            Toast.makeText(requireContext(), "Album updated with " + selected.size() + " photos!", Toast.LENGTH_SHORT).show();
            albumCreationViewModel.clear();
            Navigation.findNavController(view).popBackStack();
        });
    }

    private void showDeleteConfirmation(View view) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Album")
                .setMessage("Are you sure you want to delete this album?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    albumViewModel.deleteAlbum(currentAlbum.getId());
                    Toast.makeText(requireContext(), "Album deleted", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(view).popBackStack();
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
