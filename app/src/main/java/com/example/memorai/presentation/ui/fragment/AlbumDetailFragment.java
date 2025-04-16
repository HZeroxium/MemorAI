package com.example.memorai.presentation.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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
            albumViewModel.loadAlbumById(albumId);
        }

        setupToolbar(view);
        setupRecyclerView();
        observeViewModels();
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
                Fragment prev = getParentFragmentManager().findFragmentByTag("SecurityFragment");
                if (prev != null && prev.isAdded()) {
                    return;
                }
                updateMenuVisibility(album.isPrivate());
                displayAlbumInfo(album.getId(), album.getCreatedAt(), album.getName());
                albumViewModel.getIsAuthenticated().observe(getViewLifecycleOwner(), isAuthenticated -> {
                    if (album.isPrivate() && !isAuthenticated) {
                        openSecurityFragment(album);
                    } else {
                        loadAlbumPhotos(isAuthenticated);
                    }
                });
            }
        });
    }

    private void updateMenuVisibility(boolean isPrivate) {
        if (isPrivate) {
            binding.toolbarAlbumDetail.getMenu().clear(); // Xóa toàn bộ menu
        } else {
            // Đảm bảo menu được inflate và set listener
            binding.toolbarAlbumDetail.getMenu().clear();
            binding.toolbarAlbumDetail.inflateMenu(R.menu.menu_album_detail);
            binding.toolbarAlbumDetail.setOnMenuItemClickListener(this::onMenuItemClick);
        }
    }

    private void loadAlbumPhotos(boolean showPrivatePhotos) {
        photoViewModel.observePhotosByAlbum(albumId, showPrivatePhotos)
                .observe(getViewLifecycleOwner(), photos -> {
                    if (photos != null && !photos.isEmpty()) {
                        photoAdapter.submitList(photos);
                        binding.recyclerViewAlbumPhotos.setVisibility(View.VISIBLE);
                        binding.textViewNoPhotos.setVisibility(View.GONE);
                    } else {
                        binding.recyclerViewAlbumPhotos.setVisibility(View.GONE);
                        binding.textViewNoPhotos.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void displayAlbumInfo(String id, long createdAt, String name) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        String formattedDate = sdf.format(new Date(createdAt));

        String info = getString(R.string.album_info, id, formattedDate, name);
        binding.textViewAlbumInfo.setText(info);
    }

    private boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit_album) {
            navigateToEditAlbum();
            return true;
        } else if (id == R.id.action_album_options) {
            Toast.makeText(requireContext(), R.string.toast_album_options, Toast.LENGTH_SHORT).show();
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
            builder.setTitle(getString(R.string.delete_album));
            builder.setMessage(getString(R.string.enter_album_name));

            final EditText input = new EditText(requireContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint(getString(R.string.album_name_hint));
            builder.setView(input);

            builder.setPositiveButton(R.string.delete, (dialog, which) -> {
                String enteredName = input.getText().toString().trim();
                if (enteredName.equals(album.getName())) {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    albumViewModel.deleteAlbum(albumId);
                    Toast.makeText(requireContext(), R.string.album_deleted, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                } else {
                    Toast.makeText(requireContext(), R.string.album_name_incorrect, Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            builder.show();
        });
    }

    private void openSecurityFragment(Album album) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) {
            Toast.makeText(requireContext(), R.string.user_not_found, Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = firebaseUser.getUid();

        SecurityFragment securityFragment = new SecurityFragment();

        if(securityFragment.isVerified()) return;
        Bundle args = new Bundle();
        args.putString("albumId", album.getId());
        args.putString("userId", userId);
        securityFragment.setArguments(args);

        securityFragment.setPinVerificationListener(() -> {
            loadAlbumPhotos(true);
            albumViewModel.setAuthenticated(true);
            securityFragment.dismiss();
        });
        securityFragment.show(getParentFragmentManager(), "SecurityFragment");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        albumViewModel.loadAlbums();
        binding = null;
    }
}
