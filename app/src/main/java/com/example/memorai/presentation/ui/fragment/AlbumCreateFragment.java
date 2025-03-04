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

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumCreateBinding;
import com.example.memorai.domain.model.Album;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumCreateFragment extends Fragment {
    // In a real scenario, you might store a list of Photo IDs the user selected
    private final List<String> selectedPhotoIds = new ArrayList<>();
    private FragmentAlbumCreateBinding binding;
    private AlbumViewModel albumViewModel;

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

        // Handle back nav
        binding.toolbarAlbumCreate.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        // "Select photos" button
        binding.buttonSelectPhotos.setOnClickListener(v -> {
            // Navigate to a screen that lists all photos for multi-selection
            Navigation.findNavController(view).navigate(R.id.albumAddPhotosFragment);
        });

        // "Add album" button
        binding.buttonAddAlbum.setOnClickListener(v -> {
            String title = binding.editTextAlbumTitle.getText().toString().trim();
            if (TextUtils.isEmpty(title)) {
                Toast.makeText(requireContext(), "Please enter an album title", Toast.LENGTH_SHORT).show();
                return;
            }
            // You could create an Album object and pass it to the ViewModel
            Album album = new Album(
                    UUID.randomUUID().toString(),
                    title,
                    "",
                    "", // coverPhotoUrl if you want
                    System.currentTimeMillis(),
                    System.currentTimeMillis()
            );
            albumViewModel.addAlbum(album.getName());
            // If you need advanced logic, call the domain use case directly
            // or albumViewModel.addAlbumObject(album);

            Toast.makeText(requireContext(), "Album created!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
