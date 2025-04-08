package com.example.memorai.presentation.ui.fragment;

import static com.example.memorai.utils.ImageUtils.convertImageToBase64;

import android.net.Uri;
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
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumCreateBinding;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumCreationViewModel;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumCreateFragment extends Fragment {

    private FragmentAlbumCreateBinding binding;
    private AlbumViewModel albumViewModel;
    private AlbumCreationViewModel albumCreationViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        albumCreationViewModel = new ViewModelProvider(requireActivity()).get(AlbumCreationViewModel.class);

        setupToolbar();
        setupRecyclerView();
        observeSelectedPhotos();
        setupButtons();
    }

    private void setupToolbar() {
        binding.toolbarAlbumCreate.setNavigationOnClickListener(v ->
                Navigation.findNavController(requireView()).popBackStack()
        );
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewSelectedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewSelectedPhotos.setAdapter(selectedPhotoAdapter);

        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            albumCreationViewModel.removePhoto(photo);
            showSnackbar("Photo removed from selection");
        });
    }

    private void observeSelectedPhotos() {
        albumCreationViewModel.getSelectedPhotos().observe(getViewLifecycleOwner(), photos -> {
            selectedPhotoAdapter.submitList(photos);
            validateForm();
        });

        binding.editTextAlbumTitle.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }
        });
    }

    private void validateForm() {
        String title = binding.editTextAlbumTitle.getText().toString().trim();
        List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();

        boolean isValid = !TextUtils.isEmpty(title) && selected != null && !selected.isEmpty();
        binding.buttonAddAlbum.setEnabled(isValid);
    }

    private void setupButtons() {
        binding.buttonSelectPhotos.setOnClickListener(v ->
                Navigation.findNavController(requireView()).navigate(R.id.albumAddPhotosFragment)
        );

        binding.buttonAddAlbum.setOnClickListener(v -> createAlbum());
    }

    private void createAlbum() {
        String name = binding.editTextAlbumTitle.getText().toString().trim();
        List<Photo> selectedPhotos = albumCreationViewModel.getSelectedPhotos().getValue();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(requireContext(), "Please enter an album title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPhotos == null || selectedPhotos.isEmpty()) {
            Toast.makeText(requireContext(), "No photos selected for album!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert selected photos to list of photo IDs
        List<String> photoIds = new ArrayList<>();
        for (Photo photo : selectedPhotos) {
            photoIds.add(photo.getId());
        }

        // Create new album
        String albumId = UUID.randomUUID().toString();
        String coverPhotoUrl = selectedPhotos.get(0).getFilePath();

        Album newAlbum = new Album(
                albumId,
                name,
                "", // description - có thể thêm trường này sau
                photoIds,
                coverPhotoUrl,
                System.currentTimeMillis(),
                System.currentTimeMillis()
        );

        // Sử dụng AlbumViewModel để tạo album
        albumViewModel.createAlbumWithPhotos(newAlbum, selectedPhotos);

        // Hiển thị thông báo và quay lại
        Toast.makeText(requireContext(), "Album created!", Toast.LENGTH_SHORT).show();
        albumCreationViewModel.clear();
        Navigation.findNavController(requireView()).popBackStack();
// =======
//             String albumId = UUID.randomUUID().toString();
//             Uri contentUri = Uri.parse(selected.get(0).getFilePath());

//             String base64 = convertImageToBase64(requireContext(), contentUri);
//             Log.d("AlbumCreateFragment", "Base64: " + base64);

//             Map<String, Object> albumData = new HashMap<>();
//             albumData.put("id", albumId);
//             albumData.put("name", name);
//             albumData.put("description", "");
//             albumData.put("photos", photoIds);
//             albumData.put("coverPhotoUrl", selected.get(0).getFilePath());
//             albumData.put("createdAt", System.currentTimeMillis());
//             albumData.put("updatedAt", System.currentTimeMillis());
//             albumData.put("isPrivate", false);

//             // Lưu vào Firestore trong Collection user_albums
//             FirebaseFirestore firestore = FirebaseFirestore.getInstance();
//             FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//             if (user == null) {
//                 Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
//                 return;
//             }
//             String userId = user.getUid();

//             CollectionReference userAlbumsRef = firestore.collection("photos")
//                     .document(userId)
//                     .collection("user_albums");

//             userAlbumsRef.document(albumId).set(albumData)
//                     .addOnSuccessListener(aVoid -> {
//                         Toast.makeText(requireContext(), "Album created!", Toast.LENGTH_SHORT).show();
//                         albumCreationViewModel.clear();
//                         Navigation.findNavController(view).popBackStack();
//                     })
//                     .addOnFailureListener(e -> {
//                         Toast.makeText(requireContext(), "Failed to save album", Toast.LENGTH_SHORT).show();
//                     });
//         });
// >>>>>>> dev/tu
    }

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Helper class to simplify TextWatcher
    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}