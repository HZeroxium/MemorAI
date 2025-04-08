package com.example.memorai.presentation.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentAlbumDetailBinding;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AlbumDetailFragment extends Fragment {
    private FragmentAlbumDetailBinding binding;
    private AlbumViewModel albumViewModel;
    private PhotoViewModel photoViewModel;

    private FirebaseUser user;

    private PhotoAdapter photoAdapter;
    private String albumId;
    private Album currentAlbum;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
                             @Nullable android.view.ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAlbumDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        if (getArguments() != null) {
            albumId = getArguments().getString("album_id", "");
        }

        setupToolbar(view);
        setupRecyclerView();
        observeViewModels();

        albumViewModel.loadAlbumById(albumId);
        // Ảnh chỉ được load sau khi xác thực thành công (ở callback)
    }

    private void setupToolbar(View view) {
        binding.toolbarAlbumDetail.setNavigationOnClickListener(v -> {
            Navigation.findNavController(view).popBackStack();
        });
        binding.toolbarAlbumDetail.setOnMenuItemClickListener(this::onMenuItemClick);
    }

    private void setupRecyclerView() {
        photoAdapter = new PhotoAdapter();
        binding.recyclerViewAlbumPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewAlbumPhotos.setAdapter(photoAdapter);

        photoAdapter.setOnPhotoClickListener((sharedView, photo) -> {
            Bundle args = new Bundle();
            args.putString("photo_id", photo.getId());
            args.putString("photo_url", photo.getFilePath());
            Navigation.findNavController(requireView()).navigate(R.id.photoDetailFragment, args);
        });
    }

    private void observeViewModels() {
        albumViewModel.getAlbumLiveData().observe(getViewLifecycleOwner(), album -> {
            if (album != null) {
                // Nếu album là private, mở SecurityFragment để xác thực
                if (true) {
                    openSecurityFragment(album);
                } else {
                    displayAlbumInfo(album.getId(), album.getCreatedAt(), album.getName());
                    filterAndDisplayPhotos(false);
                }
            }
        });
    }

    private void filterAndDisplayPhotos(boolean showPrivatePhotos) {
        photoViewModel.observePhotosByAlbum(albumId).observe(getViewLifecycleOwner(), albumPhotos -> {
            if (albumPhotos != null && !albumPhotos.isEmpty()) {
                List<Photo> filteredPhotos = new ArrayList<>();
                for (Photo photo : albumPhotos) {
                    if (photo.isPrivate() == showPrivatePhotos) {
                        filteredPhotos.add(photo);
                    }
                }

                if (!filteredPhotos.isEmpty()) {
                    photoAdapter.submitList(filteredPhotos);
                    binding.recyclerViewAlbumPhotos.setVisibility(View.VISIBLE);
                    binding.textViewNoPhotos.setVisibility(View.GONE);
                } else {
                    binding.recyclerViewAlbumPhotos.setVisibility(View.GONE);
                    binding.textViewNoPhotos.setVisibility(View.VISIBLE);
                }
            } else {
                binding.recyclerViewAlbumPhotos.setVisibility(View.GONE);
                binding.textViewNoPhotos.setVisibility(View.VISIBLE);
            }
        });
    }

    private void displayAlbumInfo(String id, long createdAt, String name) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(createdAt));

        String info = "Album ID: " + id + "\n" +
                "Created: " + formattedDate + "\n" +
                "Name: " + name;
        binding.textViewAlbumInfo.setText(info);
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_album) {
            navigateToEditAlbum();
            return true;
        } else if (id == R.id.action_album_options) {
            Toast.makeText(requireContext(), "Album options clicked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.action_delete_album) {
            showDeleteConfirmationDialog();
            return true;
        }
        return false;
    }

    private void navigateToEditAlbum() {
        Bundle args = new Bundle();
        args.putString("album_id", albumId);
        Navigation.findNavController(requireView()).navigate(R.id.albumUpdateFragment, args);
    }

    private void showDeleteConfirmationDialog() {
        albumViewModel.getAlbumLiveData().observe(getViewLifecycleOwner(), album -> {
            if (album == null) return;

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Delete Album");
            builder.setMessage("Enter the album name to confirm deletion:");

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint("Album Name");
            builder.setView(input);

            builder.setPositiveButton("Delete", (dialog, which) -> {
                String enteredName = input.getText().toString().trim();
                if (enteredName.equals(album.getName())) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    albumViewModel.deleteAlbum(albumId);
                    Toast.makeText(requireContext(), "Album deleted", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(requireContext(), "Album name incorrect", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void openSecurityFragment(Album album) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }
        // Sử dụng userId (String)
        String userId = firebaseUser.getUid();

        SecurityFragment securityFragment = new SecurityFragment();
        Bundle args = new Bundle();
        args.putString("albumId", album.getId());
        args.putString("userId", userId);
        securityFragment.setArguments(args);

        // Đặt callback từ SecurityFragment (sẽ được gọi khi PIN hoặc Biometric xác thực thành công)
        securityFragment.setPinVerificationListener(() -> {
            displayAlbumInfo(album.getId(), album.getCreatedAt(), album.getName());
            filterAndDisplayPhotos(true); // Hiển thị ảnh riêng tư sau khi xác thực thành công
        });
        securityFragment.show(getParentFragmentManager(), "SecurityFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
