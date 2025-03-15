// presentation/ui/fragment/AlbumDetailFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumDetailBinding;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumDetailFragment extends Fragment {
    private FragmentAlbumDetailBinding binding;
    private AlbumViewModel albumViewModel;
    private PhotoViewModel photoViewModel;
    private PhotoAdapter photoAdapter;
    private String albumId;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        if (getArguments() != null) {
            albumId = getArguments().getString("album_id", "");
        }

        binding.toolbarAlbumDetail.setNavigationOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });

        binding.toolbarAlbumDetail.setOnMenuItemClickListener(this::onMenuItemClick);

        // Setup RecyclerView
        photoAdapter = new PhotoAdapter();
        binding.recyclerViewAlbumPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewAlbumPhotos.setAdapter(photoAdapter);

        // Handle photo click event to navigate to PhotoDetailFragment
        photoAdapter.setOnPhotoClickListener((sharedView, photo) -> {
            Bundle args = new Bundle();
            args.putString("photo_id", photo.getId());
            args.putString("photo_url", photo.getFilePath());
            Navigation.findNavController(view).navigate(R.id.photoDetailFragment, args);
        });

        loadAlbumInfo();
        loadAlbumPhotos();
    }


    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_album) {
            Bundle args = new Bundle();
            args.putString("album_id", albumId);
//            Navigation.findNavController(requireView()).navigate(R.id.albumUpdateFragment, args);
            return true;
        } else if (id == R.id.action_album_options) {
            Toast.makeText(requireContext(), "Album options clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_delete_album) {
            showDeleteConfirmationDialog();
            return true;
        }
        return false;
    }


    private void loadAlbumInfo() {
        albumViewModel.getAlbumById(albumId).observe(getViewLifecycleOwner(), album -> {
            if (album != null) {
                // Format creation date as dd-MM-yyyy
                long createdMillis = album.getCreatedAt();
                Date date = new Date(createdMillis);
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                String formattedDate = sdf.format(date);

                // Display album info
                String info = "Album ID: " + album.getId() + "\n" +
                        "Created: " + formattedDate + "\n" +
                        "Name: " + album.getName();
                binding.textViewAlbumInfo.setText(info);
            }
        });
    }

    private void loadAlbumPhotos() {
        // Already setting GridLayoutManager(3) in onViewCreated
        photoViewModel.observePhotosByAlbum().observe(getViewLifecycleOwner(), albumPhotos -> {
            if (albumPhotos != null && !albumPhotos.isEmpty()) {
                // Show photos
                photoAdapter.submitList(albumPhotos);
                binding.recyclerViewAlbumPhotos.setVisibility(View.VISIBLE);
                binding.textViewNoPhotos.setVisibility(View.GONE);
            } else {
                // No photos
                binding.recyclerViewAlbumPhotos.setVisibility(View.GONE);
                binding.textViewNoPhotos.setVisibility(View.VISIBLE);
            }
        });
        photoViewModel.loadPhotosByAlbum(albumId);
    }


    private void showDeleteConfirmationDialog() {
        albumViewModel.getAlbumById(albumId).observe(getViewLifecycleOwner(), album -> {
            if (album == null) return; // Ensure album exists

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Delete Album");
            builder.setMessage("Enter the album name to confirm deletion:");

            // Input field for confirmation
            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Album Name");
            builder.setView(input);

            // Confirm button
            builder.setPositiveButton("Delete", (dialog, which) -> {
                String enteredName = input.getText().toString().trim();
                if (enteredName.equals(album.getName())) {
                    albumViewModel.deleteAlbum(albumId);
                    Toast.makeText(requireContext(), "Album deleted", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Album name incorrect", Toast.LENGTH_SHORT).show();
                }
            });

            // Cancel button
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

            builder.show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}