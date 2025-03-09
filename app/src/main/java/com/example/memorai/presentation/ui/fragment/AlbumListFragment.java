// presentation/ui/fragment/AlbumListFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
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

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumListFragment extends Fragment {
    private FragmentAlbumListBinding binding;
    private AlbumViewModel albumViewModel;
    private AlbumAdapter albumAdapter;

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

        // Handle album click
        albumAdapter.setOnAlbumClickListener(album -> {
            Bundle args = new Bundle();
            args.putString("album_id", album.getId());
            Navigation.findNavController(view).navigate(R.id.albumDetailFragment, args);
        });

        // ViewModel to observe albums
        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        albumViewModel.observeAllAlbums().observe(getViewLifecycleOwner(), albums ->
                albumAdapter.submitList(albums)
        );

        albumViewModel.loadAllAlbums();

        // Handle Floating Action Button click (to add new album)
        binding.fabAddAlbum.setOnClickListener(v -> {
            // Navigate to create album screen
            Navigation.findNavController(view).navigate(R.id.albumCreateFragment);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}