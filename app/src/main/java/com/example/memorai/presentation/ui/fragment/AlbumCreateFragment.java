// presentation/ui/fragment/AlbumCreateFragment.java
package com.example.memorai.presentation.ui.fragment;

import static com.example.memorai.utils.ImageUtils.convertImageToBase64;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * Fragment to create a new album.
 */
@AndroidEntryPoint
public class AlbumCreateFragment extends Fragment {

    private FragmentAlbumCreateBinding binding;
    private AlbumViewModel albumViewModel;
    private AlbumCreationViewModel albumCreationViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumCreateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        albumCreationViewModel = new ViewModelProvider(requireActivity()).get(AlbumCreationViewModel.class);

        binding.toolbarAlbumCreate.setNavigationOnClickListener(v ->
                Navigation.findNavController(view).popBackStack()
        );

        setupRecyclerView();
        observeSelectedPhotos();
        setupButtons(view);
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

    private void showSnackbar(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    private void validateForm() {
        String title = binding.editTextAlbumTitle.getText().toString().trim();
        List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();

        boolean isValid = !TextUtils.isEmpty(title) && selected != null && !selected.isEmpty();
        binding.buttonAddAlbum.setEnabled(isValid);
    }

    private void setupButtons(View view) {
        binding.buttonSelectPhotos.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.albumAddPhotosFragment));

        binding.buttonAddAlbum.setOnClickListener(v -> {
            String name = binding.editTextAlbumTitle.getText().toString().trim();
            if (TextUtils.isEmpty(name)) {
                Toast.makeText(requireContext(), "Please enter an album title", Toast.LENGTH_SHORT).show();
                return;
            }

            List<Photo> selected = albumCreationViewModel.getSelectedPhotos().getValue();
            if (selected == null || selected.isEmpty()) {
                Toast.makeText(requireContext(), "No photos selected for album!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy danh sách ID ảnh
            List<String> photoIds = new ArrayList<>();
            for (Photo photo : selected) {
                photoIds.add(photo.getId());  // Đảm bảo Photo có trường ID
            }

            String albumId = UUID.randomUUID().toString();
            Uri contentUri = Uri.parse(selected.get(0).getFilePath());

            String base64 = convertImageToBase64(requireContext(), contentUri);
            Log.d("AlbumCreateFragment", "Base64: " + base64);

            Map<String, Object> albumData = new HashMap<>();
            albumData.put("id", albumId);
            albumData.put("name", name);
            albumData.put("description", "");
            albumData.put("photos", photoIds);
            albumData.put("coverPhotoUrl", selected.get(0).getFilePath());
            albumData.put("createdAt", System.currentTimeMillis());
            albumData.put("updatedAt", System.currentTimeMillis());

            // Lưu vào Firestore trong Collection user_albums
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(requireContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = user.getUid();

            CollectionReference userAlbumsRef = firestore.collection("photos")
                    .document(userId)
                    .collection("user_albums");

            userAlbumsRef.document(albumId).set(albumData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(requireContext(), "Album created!", Toast.LENGTH_SHORT).show();
                        albumCreationViewModel.clear();
                        Navigation.findNavController(view).popBackStack();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to save album", Toast.LENGTH_SHORT).show();
                    });
        });
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}