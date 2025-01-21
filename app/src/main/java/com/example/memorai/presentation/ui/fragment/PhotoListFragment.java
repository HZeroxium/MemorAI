// presentation/ui/fragment/PhotoListFragment.java
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
import com.example.memorai.databinding.FragmentPhotoListBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.util.List;

public class PhotoListFragment extends Fragment {

    private FragmentPhotoListBinding binding;
    private PhotoViewModel photoViewModel;
    private PhotoAdapter photoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup RecyclerView
        photoAdapter = new PhotoAdapter();
        binding.recyclerViewPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewPhotos.setAdapter(photoAdapter);

        // Set up photo click listener
        photoAdapter.setOnPhotoClickListener((sharedView, photo) -> {
            Bundle args = new Bundle();
            args.putString("photo_url", photo.getUrl());
            Navigation.findNavController(view).navigate(R.id.photoDetailFragment, args);
        });

        // Handle Add Photo Button
        binding.fabAddPhoto.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.addPhotoFragment)
        );

        // ViewModel to observe photos
        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), this::updatePhotos);
        photoViewModel.loadAllPhotos();
    }


    private void updatePhotos(List<Photo> photos) {
        photoAdapter.submitList(photos);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
