// presentation/ui/fragment/AlbumListFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.memorai.databinding.FragmentAlbumListBinding;
import com.example.memorai.domain.model.Album;
import com.example.memorai.presentation.ui.adapter.AlbumAdapter;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

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

        albumAdapter = new AlbumAdapter();
        binding.recyclerViewAlbums.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewAlbums.setAdapter(albumAdapter);

        albumAdapter.setOnAlbumClickListener(album -> openAlbumDetails(album));

        binding.fabAddAlbum.setOnClickListener(v -> showAddAlbumDialog());

        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        albumViewModel.observeAllAlbums().observe(getViewLifecycleOwner(), albums -> {
            albumAdapter.submitList(albums);
        });
        albumViewModel.loadAllAlbums();
    }

    private void openAlbumDetails(Album album) {
        // Possible next fragment or detail screen for the album
        // e.g. show photos from that album
    }

    private void showAddAlbumDialog() {
        EditText editText = new EditText(requireContext());
        editText.setInputType(InputType.TYPE_CLASS_TEXT);

        new AlertDialog.Builder(requireContext())
                .setTitle("Create Album")
                .setView(editText)
                .setPositiveButton("OK", (dialog, which) -> {
                    String albumName = editText.getText().toString().trim();
                    if (!albumName.isEmpty()) {
                        albumViewModel.addAlbum(albumName);
                    }
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
