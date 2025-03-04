// presentation/ui/fragment/AlbumDetailFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumDetailBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.util.List;

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
        binding.recyclerViewAlbumPhotos.setAdapter(photoAdapter);

        // Observe album info or load album details
        // For example, if you have a method to get a single album:
        // albumViewModel.observeAlbumById(albumId).observe(getViewLifecycleOwner(), album -> {...});
        // Or you just load photos from the album
        loadAlbumInfo();
        loadAlbumPhotos();
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_album) {
            // Navigate to an edit screen
            Bundle args = new Bundle();
            args.putString("album_id", albumId);
            Navigation.findNavController(requireView()).navigate(R.id.albumCreateFragment, args);
            return true;
        } else if (id == R.id.action_album_options) {
            Toast.makeText(requireContext(), "Album options clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_delete_album) {
            albumViewModel.deleteAlbum(albumId);
            Toast.makeText(requireContext(), "Album deleted", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(requireView()).popBackStack();
            return true;
        }
        return false;
    }

    private void loadAlbumInfo() {
        // If your ViewModel has a method to get a single album:
        // e.g. Album album = albumViewModel.getAlbumById(albumId);
        // Then display:
        // binding.textViewAlbumInfo.setText("Created: ...\nName: ...");
        // For now, a placeholder:
        binding.textViewAlbumInfo.setText("Album ID: " + albumId + "\n<created date>\n<album name>");
    }

    private void loadAlbumPhotos() {
        // Use your PhotoViewModel to get photos by album
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), photos -> {
            // Filter by albumId if needed
            List<Photo> albumPhotos = photoViewModel.getPhotosByAlbum(albumId);
            photoAdapter.submitList(albumPhotos);
        });
        photoViewModel.loadPhotosByAlbum(albumId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
