// presentation/ui/fragment/AlbumListFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumListBinding;
import com.example.memorai.presentation.ui.adapter.AlbumAdapter;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumListFragment extends Fragment {
    private FragmentAlbumListBinding binding;
    private AlbumViewModel albumViewModel;

    private PhotoViewModel photoViewModel;
    private AlbumAdapter albumAdapter;

    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup Toolbar
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack(); // Back navigation
        });

        // Setup RecyclerView with Grid Layout
        albumAdapter = new AlbumAdapter();
        binding.recyclerViewAlbums.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewAlbums.setAdapter(albumAdapter);
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        // Handle album click
        albumAdapter.setOnAlbumClickListener(album -> {
            Bundle args = new Bundle();
            args.putString("album_id", album.getId());
            photoViewModel.clearAlbumPhoto();
            albumViewModel.clearAlbum();
            Navigation.findNavController(view).navigate(R.id.albumDetailFragment, args);
        });

        // ViewModel to observe albums
        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        user = FirebaseAuth.getInstance().getCurrentUser();

        albumViewModel.getAlbums().observe(getViewLifecycleOwner(), albums ->
                albumAdapter.submitList(new ArrayList<>(albums))
        );

        // Handle Floating Action Button click (to add new album)
        binding.fabAddAlbum.setOnClickListener(v -> {
            // Navigate to create album screen
            Navigation.findNavController(view).navigate(R.id.albumCreateFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (albumAdapter != null) {
            albumAdapter.setOnAlbumClickListener(null);
        }
        binding.recyclerViewAlbums.setAdapter(null);
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        albumAdapter = null;
    }
}