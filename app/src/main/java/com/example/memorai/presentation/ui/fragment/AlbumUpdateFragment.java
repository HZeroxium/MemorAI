// presentation/ui/fragment/AlbumUpdateFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumUpdateBinding;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumCreationViewModel;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment to update an existing album.
 */
@AndroidEntryPoint
public class AlbumUpdateFragment extends Fragment {

    private FragmentAlbumUpdateBinding binding;
    private AlbumViewModel albumViewModel;
    private AlbumCreationViewModel albumCreationViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;
    private Album currentAlbum;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumUpdateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        albumCreationViewModel = new ViewModelProvider(requireActivity()).get(AlbumCreationViewModel.class);

        setupToolbar(view);
        setupRecyclerView();
        setupObservers();
        setupButtons(view);
    }

    private void setupToolbar(View view) {
        binding.toolbarAlbumUpdate.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewSelectedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewSelectedPhotos.setAdapter(selectedPhotoAdapter);

        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            albumCreationViewModel.removePhoto(photo);
            showSnackbar("Photo removed");
        });
    }

    private void setupObservers() {
        albumCreationViewModel.getSelectedPhotos().observe(getViewLifecycleOwner(), photos -> {
            selectedPhotoAdapter.submitList(photos);
            validateForm();
        });

        binding.editTextAlbumTitle.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private void validateForm() {
        String title = binding.editTextAlbumTitle.getText().toString().trim();
        List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();

        boolean isValid = !TextUtils.isEmpty(title) && selected != null && !selected.isEmpty();
        binding.buttonUpdateAlbum.setEnabled(isValid);
    }

    private void setupButtons(View view) {
        binding.buttonSelectPhotos.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.albumAddPhotosFragment));

        binding.buttonUpdateAlbum.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Album Updated!", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(view).popBackStack();
        });
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
