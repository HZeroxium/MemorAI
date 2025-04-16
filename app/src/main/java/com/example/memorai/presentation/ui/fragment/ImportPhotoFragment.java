package com.example.memorai.presentation.ui.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentImportPhotoBinding;
import com.example.memorai.domain.model.Notification;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.NotificationViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.example.memorai.utils.ImageClassifierHelper;
import com.example.memorai.utils.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ImportPhotoFragment extends Fragment {

    private final List<Photo> importedPhotos = new ArrayList<>();
    private FragmentImportPhotoBinding binding;
    private PhotoViewModel photoViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;
    private ImageClassifierHelper imageClassifier;

    private NotificationViewModel notificationViewModel;

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetMultipleContents(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;
                            requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                        } catch (SecurityException e) {
                            uri = copyImageToAppStorage(uri);
                        }

                        Bitmap bitmap = getBitmapFromUri(uri);
                        if (bitmap == null) {
                            Toast.makeText(requireContext(), R.string.error_loading_image, Toast.LENGTH_SHORT).show();
                            continue;
                        }

                        // Nén và resize ảnh
                        Bitmap compressedBitmap = compressAndResizeBitmap(bitmap);

                        // Tạo đối tượng Photo tạm thời để hiển thị trong RecyclerView
                        String photoId = UUID.randomUUID().toString();
                        List<String> tags = new ArrayList<>();
                        Photo photo = new Photo(photoId, bitmapToBase64(compressedBitmap), tags,
                                System.currentTimeMillis(), System.currentTimeMillis());
                        photo.setBitmap(compressedBitmap);

                        // Thêm vào danh sách importedPhotos để hiển thị
                        importedPhotos.add(photo);

                        // Cập nhật RecyclerView với danh sách importedPhotos
                        selectedPhotoAdapter.submitList(new ArrayList<>(importedPhotos));
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.no_photos_selected, Toast.LENGTH_SHORT).show();
                }
            });

    private Bitmap getBitmapFromUri(Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return rotateBitmapIfNeeded(bitmap, uri);
        } catch (Exception e) {
            Log.e("ImportPhotoFragment", "Error loading bitmap", e);
            return null;
        }
    }

    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(inputStream);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);

            if (rotationInDegrees != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationInDegrees);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            Log.e("ImportPhotoFragment", "Error rotating bitmap", e);
        }
        return bitmap;
    }

    private int exifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }

    private Bitmap compressAndResizeBitmap(Bitmap originalBitmap) {
        int maxDimension = 800;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float scale = Math.min((float) maxDimension / width, (float) maxDimension / height);

        if (scale < 1) {
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        }
        return originalBitmap;
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentImportPhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        notificationViewModel = new ViewModelProvider(requireActivity()).get(NotificationViewModel.class);
        setupRecyclerView();
        binding.buttonSelectFromGallery.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        binding.buttonConfirmImport.setOnClickListener(v -> confirmImport());
        binding.toolbarImportPhoto.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        imageClassifier = new ImageClassifierHelper(requireContext());

    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewImportedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewImportedPhotos.setAdapter(selectedPhotoAdapter);

        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            importedPhotos.remove(photo);
            selectedPhotoAdapter.submitList(new ArrayList<>(importedPhotos));
        });
    }

    private void confirmImport() {
        if (importedPhotos.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_photos_imported, Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_import_title)
                .setMessage(getString(R.string.confirm_import_message, importedPhotos.size()))
                .setPositiveButton(R.string.confirm_button, (dialog, which) -> {
                    List<Photo> photosToSave = new ArrayList<>(importedPhotos);
                    classifyAndSavePhotos(photosToSave);
                    importedPhotos.clear();
                    selectedPhotoAdapter.submitList(new ArrayList<>(importedPhotos));
                    binding.buttonConfirmImport.setEnabled(false); // Vô hiệu hóa nút sau khi xác nhận
                    String notificationId = UUID.randomUUID().toString();
                    Notification notification = new Notification(
                            notificationId,
                            getString(R.string.photos_saved_success),
                            getString(R.string.photos_saved_success),
                            System.currentTimeMillis()
                    );
                    notificationViewModel.sendNotification(notification);
                    NavController navController = Navigation.findNavController(requireView());
                    navController.navigateUp();
                })
                .setNegativeButton(R.string.cancel_button, null)
                .show();
    }

    private void classifyAndSavePhotos(List<Photo> photos) {
        Toast.makeText(requireContext(), R.string.analyzing_photos, Toast.LENGTH_SHORT).show();

        for (Photo photo : photos) {
            Bitmap bitmap = photo.getBitmap();
            if (bitmap != null) {
                List<String> tags = imageClassifier.classify(bitmap);
                photo.setTags(tags);
                Log.d("TakePhotoFragment", "Photo tagged with: " + tags);

                // Lưu ảnh đã nén qua PhotoViewModel
                photoViewModel.addPhoto(bitmap, tags);
            }
        }

        Toast.makeText(requireContext(), R.string.photos_saved_success, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Reset danh sách khi fragment bị hủy
        if (selectedPhotoAdapter != null) {
            selectedPhotoAdapter.submitList(new ArrayList<>());
        }
        if (binding != null) {
            binding.recyclerViewImportedPhotos.setAdapter(null);
            binding = null;
        }

        if (imageClassifier != null) {
            imageClassifier.close();
            imageClassifier = null;
        }
    }

    private Uri copyImageToAppStorage(Uri sourceUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
            if (inputStream == null)
                return sourceUri;

            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File newFile = new File(storageDir, "IMG_" + System.currentTimeMillis() + ".jpg");

            OutputStream outputStream = new FileOutputStream(newFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            outputStream.close();

            return FileProvider.getUriForFile(requireContext(), "com.example.memorai.fileprovider", newFile);
        } catch (IOException e) {
            e.printStackTrace();
            return sourceUri;
        }
    }
}