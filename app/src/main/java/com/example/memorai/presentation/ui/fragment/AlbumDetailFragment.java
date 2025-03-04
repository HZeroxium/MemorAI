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
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumDetailBinding;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

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
        albumViewModel.getAlbumById(albumId).observe(getViewLifecycleOwner(), album -> {
            if (album != null) {
                String info = "Album ID: " + album.getId() + "\n" +
                        "Created: " + album.getCreatedAt() + "\n" +
                        "Name: " + album.getName();
                binding.textViewAlbumInfo.setText(info);
            }
        });
    }


    private void loadAlbumPhotos() {
        // Ensure LayoutManager is set
        binding.recyclerViewAlbumPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        photoViewModel.observePhotosByAlbum().observe(getViewLifecycleOwner(), albumPhotos -> {
            if (albumPhotos != null && !albumPhotos.isEmpty()) {
                photoAdapter.submitList(albumPhotos);
                binding.textViewNoPhotos.setVisibility(View.GONE);
            } else {
                binding.textViewNoPhotos.setVisibility(View.VISIBLE);
            }
        });

        // Ensure data is loaded properly
        photoViewModel.loadPhotosByAlbum(albumId);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
