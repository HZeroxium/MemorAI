package com.example.memorai.presentation.ui.fragment;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentTakePhotoBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.example.memorai.utils.ImageUtils;
import com.example.memorai.utils.ImageClassifierHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import dagger.hilt.android.AndroidEntryPoint;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@AndroidEntryPoint
public class TakePhotoFragment extends Fragment {

    private FragmentTakePhotoBinding binding;
    private PhotoViewModel photoViewModel;
    private SelectedPhotoAdapter selectedPhotoAdapter;
    private Uri currentPhotoUri;
    private ImageClassifierHelper imageClassifier;

    private final List<Photo> tempPhotoList = new ArrayList<>();

    private final ActivityResultLauncher<Uri> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(), success -> {
                if (success && currentPhotoUri != null) {
                    Bitmap bitmap = getBitmapFromUri(currentPhotoUri);
                    if (bitmap != null) {
                        // Giảm kích thước và nén ảnh trước khi chuyển thành base64
                        Bitmap compressedBitmap = compressAndResizeBitmap(bitmap);
                        String base64Image = bitmapToBase64(compressedBitmap);
                        Photo photo = new Photo(UUID.randomUUID().toString(), base64Image,
                                new ArrayList<>(), System.currentTimeMillis(), System.currentTimeMillis());
                        photo.setBitmap(compressedBitmap);
                        tempPhotoList.add(photo);
                        selectedPhotoAdapter.submitList(new ArrayList<>(tempPhotoList));
                    }
                } else {
                    Toast.makeText(requireContext(), R.string.failed_to_capture_photo, Toast.LENGTH_SHORT).show();
                }
            });

    private Bitmap getBitmapFromUri(Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return rotateBitmapIfNeeded(bitmap, uri);
        } catch (Exception e) {
            Log.e("TakePhotoFragment", "Error loading bitmap", e);
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
            Log.e("TakePhotoFragment", "Error rotating bitmap", e);
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

    // Hàm mới: Giảm kích thước và nén ảnh
    private Bitmap compressAndResizeBitmap(Bitmap originalBitmap) {
        // Giảm kích thước ảnh xuống tối đa 800px chiều dài hoặc rộng (tùy theo tỷ lệ)
        int maxDimension = 800;
        int width = originalBitmap.getWidth();
        int height = originalBitmap.getHeight();
        float scale = Math.min((float) maxDimension / width, (float) maxDimension / height);

        if (scale < 1) {
            int newWidth = Math.round(width * scale);
            int newHeight = Math.round(height * scale);
            return Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true);
        }
        return originalBitmap; // Không cần resize nếu ảnh đã nhỏ
    }

    // Hàm mới: Chuyển Bitmap thành base64 với nén
    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream); // Nén xuống 70% chất lượng
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(requireContext(), R.string.camera_permission_denied, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (currentPhotoUri != null) {
            outState.putParcelable("currentPhotoUri", currentPhotoUri);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentTakePhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            currentPhotoUri = savedInstanceState.getParcelable("currentPhotoUri");
        }
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        setupRecyclerView();
        binding.buttonCapture.setOnClickListener(v -> openCamera());
        binding.buttonConfirm.setOnClickListener(v -> confirmPhotos());
        binding.toolbarTakePhoto.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        imageClassifier = new ImageClassifierHelper(requireContext());
    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewCapturedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewCapturedPhotos.setAdapter(selectedPhotoAdapter);

        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            // Don't try to delete from database - these are temporary photos
            // that haven't been saved to the database yet
            tempPhotoList.remove(photo);
            selectedPhotoAdapter.submitList(new ArrayList<>(tempPhotoList));
        });
    }

    private File createImageFile() {
        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(storageDir, fileName);
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            File imageFile = createImageFile();
            currentPhotoUri = FileProvider.getUriForFile(requireContext(), "com.example.memorai.fileprovider",
                    imageFile);
            cameraLauncher.launch(currentPhotoUri);
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void confirmPhotos() {
        if (tempPhotoList.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_photos_captured, Toast.LENGTH_SHORT).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.confirm_title)
                .setMessage(getString(R.string.confirm_message, tempPhotoList.size()))
                .setPositiveButton(R.string.confirm_button, (dialog, which) -> {
                    List<Photo> photosToSave = new ArrayList<>(tempPhotoList);
                    classifyAndSavePhotos(photosToSave);
                    tempPhotoList.clear();
                    selectedPhotoAdapter.submitList(new ArrayList<>(tempPhotoList));
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
        requireActivity().onBackPressed();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.recyclerViewCapturedPhotos.setAdapter(null);
            binding = null;
        }

        if (imageClassifier != null) {
            imageClassifier.close();
            imageClassifier = null;
        }
    }
}