// presentation/ui/fragment/CollectionsFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.memorai.databinding.FragmentCollectionsBinding;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CollectionsFragment extends Fragment {
    private FragmentCollectionsBinding binding;
    private AlbumViewModel albumViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCollectionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);

        // Possibly load all user albums or special "favorites/trash" logic
        // binding.recyclerViewAllAlbums.setAdapter(...)

        // Example button clicks:
        binding.buttonFavorites.setOnClickListener(v -> {
            // show favorites
        });
        binding.buttonTrash.setOnClickListener(v -> {
            // show trash
        });
        binding.buttonArchive.setOnClickListener(v -> {
            // show archive
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
