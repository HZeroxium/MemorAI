// presentation/ui/fragment/PhotoDetailFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.view.MenuItem;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
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

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentPhotoDetailBinding;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoDetailFragment extends Fragment {

    private FragmentPhotoDetailBinding binding;
    private PhotoViewModel photoViewModel;
    private byte[] photoUrl;
    private String photoId;
    private boolean isPrivate = false;

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

        if (getArguments() != null) {
            photoUrl = getArguments().getByteArray("photo_url");
            if (photoUrl != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(photoUrl, 0, photoUrl.length);
            }
            photoId = getArguments().getString("photo_id", "");
        }

        // ViewModel
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(view).popBackStack());

        setSharedElementTransition();

        // Attach menu to toolbar
        binding.toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_private) {
                togglePrivate();
                return true;
            }

            if (item.getItemId() == R.id.action_share_photo) {
                sharePhoto(photoUrl);
                return true;
            }

            if (item.getItemId() == R.id.action_edit_photo) {
                Bundle args = new Bundle();
                args.putByteArray("photo_bitmap", photoUrl);  // Đúng kiểu dữ liệu
                Navigation.findNavController(requireView()).navigate(R.id.editPhotoFragment, args);
                return true;
            }

            if (item.getItemId() == R.id.action_delete_photo) {
                confirmDeletePhoto();
                return true;
            }

            return false;
        });

        if (photoUrl != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(photoUrl, 0, photoUrl.length);
            Glide.with(this)
                    .load(bitmap) // Load bằng Bitmap
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(binding.imageViewDetailPhoto);
        }

    }

    private void sharePhoto(byte[] photoByteArray) {
        if (photoByteArray == null) {
            Toast.makeText(requireContext(), "No photo to share", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File file = new File(requireContext().getCacheDir(), "shared_photo.png");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(photoByteArray);
            fos.flush();
            fos.close();

            Uri uri = FileProvider.getUriForFile(requireContext(), "com.example.memorai.fileprovider", file);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to share photo", Toast.LENGTH_SHORT).show();
        }
    }


    private void setSharedElementTransition() {
        setSharedElementEnterTransition(
                new androidx.transition.TransitionSet()
                        .addTransition(new androidx.transition.ChangeBounds())
                        .addTransition(new androidx.transition.ChangeTransform())
                        .addTransition(new androidx.transition.ChangeImageTransform())
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Thêm phương thức togglePrivate
    private void togglePrivate() {
        isPrivate = !isPrivate;

        // Gọi ViewModel để cập nhật trạng thái
        photoViewModel.setPhotoPrivacy(photoId, isPrivate);

        // Hiển thị thông báo
        Toast.makeText(requireContext(),
                isPrivate ? "Photo set to private" : "Photo set to public",
                Toast.LENGTH_SHORT).show();

        // Cập nhật icon (nếu cần)
        updatePrivateIcon();
    }

    // Thêm phương thức cập nhật icon
    private void updatePrivateIcon() {
        MenuItem privateItem = binding.toolbar.getMenu().findItem(R.id.action_private);
        if (privateItem != null) {
            privateItem.setIcon(isPrivate ?
                    R.drawable.ic_lock :
                    R.drawable.ic_lock_open);
            privateItem.setTitle(isPrivate ?
                    "Set Public" : "Set Private");
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
                args.putByteArray("photo_bitmap", photoUrl);  // Đúng kiểu dữ liệu
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
