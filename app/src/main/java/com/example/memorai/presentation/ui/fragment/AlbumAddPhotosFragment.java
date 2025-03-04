// presentation/ui/fragment/AlbumAddPhotosFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.databinding.FragmentAlbumAddPhotosBinding;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumAddPhotosFragment extends Fragment {
    private FragmentAlbumAddPhotosBinding binding;
    private PhotoViewModel photoViewModel;
    private PhotoAdapter photoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumAddPhotosBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        binding.toolbarAddPhotos.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });

        // Setup RecyclerView
        photoAdapter = new PhotoAdapter();
        photoAdapter.setSelectionMode(true); // Force checkboxes
        binding.recyclerViewAllPhotosForSelection.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewAllPhotosForSelection.setAdapter(photoAdapter);

        // Observe all photos
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), photos -> {
            photoAdapter.submitList(photos);
        });
        photoViewModel.loadAllPhotos();

        // If user types to search photos:
        binding.editTextSearchPhotos.setOnEditorActionListener((textView, actionId, event) -> {
            String query = textView.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                photoViewModel.searchPhotos(query);
            } else {
                photoViewModel.loadAllPhotos();
            }
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
