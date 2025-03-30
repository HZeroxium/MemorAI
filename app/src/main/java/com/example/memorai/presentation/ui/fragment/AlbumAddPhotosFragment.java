// presentation/ui/fragment/AlbumAddPhotosFragment.java
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
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumAddPhotosBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumCreationViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Allows user to pick photos to add to the new album.
 * We store these selections in a shared AlbumCreationViewModel.
 */
@AndroidEntryPoint
public class AlbumAddPhotosFragment extends Fragment {

    private FragmentAlbumAddPhotosBinding binding;
    private PhotoViewModel photoViewModel;
    private AlbumCreationViewModel albumCreationViewModel;
    private PhotoAdapter photoAdapter;

    private FirebaseUser user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumAddPhotosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        albumCreationViewModel = new ViewModelProvider(requireActivity()).get(AlbumCreationViewModel.class);

        setupToolbar();
        setupRecyclerView();
        setupSearch();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        photoViewModel.loadAllPhotos(user.getUid());

        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), photos -> {
            // 1) get old selections
            Set<String> oldSelections = new HashSet<>(photoAdapter.getSelectedPhotoIds());

            // 2) submit new list
            photoAdapter.submitList(photos);

            // 3) filter out only valid ones
            Set<String> newValidSelections = new HashSet<>();
            for (Photo p : photos) {
                if (oldSelections.contains(p.getId())) {
                    newValidSelections.add(p.getId());
                }
            }

            // 4) re-apply selection in the adapter
            photoAdapter.setSelectedPhotoIds(newValidSelections);

            // 5) also cross-check with the AlbumCreationViewModel in case user previously selected
            List<Photo> previouslySelected = albumCreationViewModel.getSelectedPhotos().getValue();
            if (previouslySelected != null) {
                for (Photo p : previouslySelected) {
                    if (photos.contains(p)) {
                        newValidSelections.add(p.getId());
                    }
                }
                photoAdapter.setSelectedPhotoIds(newValidSelections);
            }
        });

        // The "Add" or "Confirm" button
        binding.buttonConfirmSelection.setOnClickListener(v -> {
            List<Photo> all = photoAdapter.getCurrentList();
            List<Photo> selectedList = new ArrayList<>();
            for (Photo photo : all) {
                if (photoAdapter.isSelected(photo.getId())) {
                    selectedList.add(photo);
                }
            }

            if (selectedList.isEmpty()) {
                Toast.makeText(requireContext(), "No photos selected!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save these to the shared creation VM
            albumCreationViewModel.setPhotos(selectedList);

            requireActivity().onBackPressed();
        });

    }

    private void setupToolbar() {
        binding.toolbarAddPhotos.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
        binding.toolbarAddPhotos.setTitle(R.string.select_photos);
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoAdapter();
        photoAdapter.setSelectionMode(true); // Force checkboxes always visible
        binding.recyclerViewAllPhotosForSelection.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewAllPhotosForSelection.setAdapter(photoAdapter);
    }

    private void setupSearch() {
        binding.editTextSearchPhotos.setOnEditorActionListener((textView, actionId, event) -> {
            String query = textView.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                photoViewModel.searchPhotos(query);
            } else {
                photoViewModel.loadAllPhotos(user.getUid());
            }
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding.recyclerViewAllPhotosForSelection.setAdapter(null);
        binding = null;
    }
}
