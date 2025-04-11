// presentation/ui/fragment/PhotoDetailFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Environment;
import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentPhotoDetailBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoDetailFragment extends Fragment {

    private FragmentPhotoDetailBinding binding;
    private PhotoViewModel photoViewModel;
    private Bitmap photoUrl;
    private String photoId;
    private boolean isPrivate = false;
    private Photo currentPhoto;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ViewModel
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        if (getArguments() != null) {
            photoId = getArguments().getString("photo_id", "");

            // Load the complete photo details
            if (!photoId.isEmpty()) {
                photoViewModel.getPhotoById(photoId).observe(getViewLifecycleOwner(), photo -> {
                    if (photo != null) {
                        currentPhoto = photo;
                        if (photo.getBitmap() != null) {
                            Glide.with(this)
                                    .load(photo.getBitmap())
                                    .placeholder(R.drawable.placeholder_image)
                                    .error(R.drawable.error_image)
                                    .into(binding.imageViewDetailPhoto);
                        }
                        updateUI(photo);
                        isPrivate = photo.getIsPrivate(); // Update the isPrivate state
                        updatePrivateIcon(); // Update the privacy icon
                    }
                });
            }
        }

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        setSharedElementTransition();

        // Attach menu to toolbar
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_private) {
                togglePrivate();
                return true;
            }

            if (item.getItemId() == R.id.action_share_photo) {
                sharePhoto(currentPhoto.getBitmap());
                return true;
            }

            if (item.getItemId() == R.id.action_edit_photo) {
                Bundle args = new Bundle();
                args.putString("photo_id", photoId);
                Navigation.findNavController(requireView()).navigate(R.id.editPhotoFragment, args);
                return true;
            }

            if (item.getItemId() == R.id.action_delete_photo) {
                confirmDeletePhoto();
                return true;
            }

            return false;
        });

    }

    private void updateUI(Photo photo) {
        binding.textViewCreatedDate.setText(getString(R.string.created) + " " + dateFormat.format(new Date(photo.getCreatedAt())));
        binding.textViewCreatedDate.setText(getString(R.string.modified) + " " + dateFormat.format(new Date(photo.getUpdatedAt())));

        // Set privacy status
        binding.textViewPrivacyStatus.setText(photo.getIsPrivate() ? R.string.private_photo : R.string.public_photo);
        binding.textViewPrivacyStatus.setCompoundDrawablesWithIntrinsicBounds(
                photo.getIsPrivate() ? R.drawable.ic_lock : R.drawable.ic_lock_open,
                0, 0, 0);

        // Update tags
        binding.chipGroupTags.removeAllViews();
        if (photo.getTags() != null && !photo.getTags().isEmpty()) {
            binding.textViewNoTags.setVisibility(View.GONE);
            binding.chipGroupTags.setVisibility(View.VISIBLE);

            List<String> tagsToShow = photo.getTags().subList(0, Math.min(5, photo.getTags().size()));


            for (String tag : tagsToShow) {
                Chip chip = new Chip(requireContext());
                chip.setText(tag);
                chip.setClickable(false);
                chip.setChipBackgroundColorResource(R.color.blue_color_picker);
                chip.setTextColor(getResources().getColor(R.color.white, null));
                binding.chipGroupTags.addView(chip);
            }
        } else {
            binding.textViewNoTags.setVisibility(View.VISIBLE);
            binding.chipGroupTags.setVisibility(View.GONE);
        }

        // Show the details container now that we have data
        binding.containerPhotoDetails.setVisibility(View.VISIBLE);
    }

    private void sharePhoto(Bitmap bitmap) {
        if (bitmap == null) {
            Toast.makeText(requireContext(), "No photo to share", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sử dụng cả hai phương án cho thiết bị thật và giả lập
        File file;
        Uri contentUri;

        try {
            String fileName = "share_" + System.currentTimeMillis() + ".jpg";

            // Thử phương án ưu tiên (external storage)
            File externalDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (externalDir != null) {
                file = new File(externalDir, fileName);
            } else {
                // Fallback cho giả lập
                file = new File(requireContext().getFilesDir(), fileName);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            }

            // Kiểm tra xem có phải giả lập không
            boolean isEmulator = Build.FINGERPRINT.startsWith("generic")
                    || Build.FINGERPRINT.startsWith("unknown")
                    || Build.MODEL.contains("google_sdk")
                    || Build.MODEL.contains("Emulator")
                    || Build.MODEL.contains("Android SDK");

            if (isEmulator) {
                // Giả lập thường cần file:// URI thay vì content:// URI
                contentUri = Uri.fromFile(file);
            } else {
                // Thiết bị thật dùng FileProvider
                contentUri = FileProvider.getUriForFile(
                        requireContext(),
                        requireContext().getPackageName() + ".fileprovider",
                        file
                );
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Xử lý đặc biệt cho giả lập
            if (isEmulator) {
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            startActivity(Intent.createChooser(shareIntent, "Share photo"));

        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("PhotoShare", "Sharing error", e);
        }
    }

    private void setSharedElementTransition() {
        setSharedElementEnterTransition(
                new androidx.transition.TransitionSet()
                        .addTransition(new androidx.transition.ChangeBounds())
                        .addTransition(new androidx.transition.ChangeTransform())
                        .addTransition(new androidx.transition.ChangeImageTransform()));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void togglePrivate() {
        isPrivate = !isPrivate;

        // Call ViewModel to update status
        photoViewModel.setPhotoPrivacy(photoId, isPrivate);

        // Show notification
        Toast.makeText(requireContext(),
                isPrivate ? "Photo set to private" : "Photo set to public",
                Toast.LENGTH_SHORT).show();

        // Update icon and text
        updatePrivateIcon();
        binding.textViewPrivacyStatus.setText(isPrivate ? R.string.privates : R.string.public_photo);
        binding.textViewPrivacyStatus.setCompoundDrawablesWithIntrinsicBounds(
                isPrivate ? R.drawable.ic_lock : R.drawable.ic_lock_open,
                0, 0, 0);
    }

    private void updatePrivateIcon() {
        MenuItem privateItem = binding.toolbar.getMenu().findItem(R.id.action_private);
        if (privateItem != null) {
            privateItem.setIcon(isPrivate ? R.drawable.ic_lock : R.drawable.ic_lock_open);
            privateItem.setTitle(isPrivate ? R.string.set_public : R.string.set_private);
        }
    }

    private void showPopupMenu(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor, Gravity.END);
        popup.getMenuInflater().inflate(R.menu.menu_photo_detail, popup.getMenu());

        // Force icons to be shown in the popup menu
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceShowIcon.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_share_photo) {
                sharePhoto(photoUrl);
                return true;
            }
            if (item.getItemId() == R.id.action_edit_photo) {
                Bundle args = new Bundle();
                photoId = getArguments().getString("photo_id", "");
                args.putString("photo_id", photoId);
                Navigation.findNavController(requireView()).navigate(R.id.editPhotoFragment, args);
                return true;
            }
            if (item.getItemId() == R.id.action_delete_photo) {
                confirmDeletePhoto();
                return true;
            }
            return false;
        });

        popup.show();
    }

    public void confirmDeletePhoto() {
        if (photoId == null || photoId.isEmpty()) {
            Toast.makeText(requireContext(), "No photoId provided", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    photoViewModel.deletePhoto(photoId);
                    Toast.makeText(requireContext(), "Photo deleted", Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).popBackStack();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
