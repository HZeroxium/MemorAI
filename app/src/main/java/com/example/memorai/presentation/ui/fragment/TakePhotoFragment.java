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
import com.example.memorai.databinding.FragmentTakePhotoBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.SelectedPhotoAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.example.memorai.utils.ImageUtils;
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

    private final List<Photo> tempPhotoList = new ArrayList<>();

    private final ActivityResultLauncher<Uri> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && currentPhotoUri != null) {
                    Bitmap bitmap = getBitmapFromUri(currentPhotoUri);
                    if (bitmap != null) {
                        Photo photo = new Photo(UUID.randomUUID().toString(), currentPhotoUri.toString(), new ArrayList<>(), System.currentTimeMillis(), System.currentTimeMillis());
                        photo.setBitmap(bitmap);
                        tempPhotoList.add(photo); // Lưu vào danh sách tạm
                        selectedPhotoAdapter.submitList(new ArrayList<>(tempPhotoList));
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to capture photo", Toast.LENGTH_SHORT).show();
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
            case ExifInterface.ORIENTATION_ROTATE_90: return 90;
            case ExifInterface.ORIENTATION_ROTATE_180: return 180;
            case ExifInterface.ORIENTATION_ROTATE_270: return 270;
            default: return 0;
        }
    }


    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    openCamera();
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTakePhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        setupRecyclerView();
        binding.buttonCapture.setOnClickListener(v -> openCamera());
        binding.buttonConfirm.setOnClickListener(v -> confirmPhotos());
        binding.toolbarTakePhoto.setNavigationOnClickListener(v -> requireActivity().onBackPressed());


    }

    private void setupRecyclerView() {
        selectedPhotoAdapter = new SelectedPhotoAdapter();
        binding.recyclerViewCapturedPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewCapturedPhotos.setAdapter(selectedPhotoAdapter);

        selectedPhotoAdapter.setOnRemoveClickListener(photo -> {
            photoViewModel.deletePhoto(photo.getId());
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
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            File imageFile = createImageFile();
            currentPhotoUri = FileProvider.getUriForFile(requireContext(), "com.example.memorai.fileprovider", imageFile);
            cameraLauncher.launch(currentPhotoUri);
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void confirmPhotos() {
        if (tempPhotoList.isEmpty()) {
            Toast.makeText(requireContext(), "No photos captured", Toast.LENGTH_SHORT).show();
            return;
        }

        for (Photo photo : tempPhotoList) {
            photoViewModel.addPhoto(photo); // Đảm bảo ViewModel lưu ảnh
        }


        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Confirm")
                .setMessage("Upload " + tempPhotoList.size() + " photo(s) to Firestore?")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    List<Photo> photosToUpload = new ArrayList<>(tempPhotoList); // Copy list để tránh bị clear()
                    uploadPhotos(photosToUpload);
                    tempPhotoList.clear(); // Chỉ xóa sau khi lấy danh sách xong
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    private void uploadPhotos(List<Photo> photos) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        CollectionReference userPhotosRef = firestore.collection("photos").document(userId).collection("user_photos");

        for (Photo p : photos) {
            Uri contentUri = Uri.parse(p.getFilePath());
            String base64Image = ImageUtils.convertImageToBase64(requireContext(), contentUri);

            if (base64Image == null) {
                Log.e("TakePhotoFragment", "Failed to convert image to Base64");
                Toast.makeText(requireContext(), "Error converting image", Toast.LENGTH_SHORT).show();
                continue;
            }

            Map<String, Object> photoData = new HashMap<>();
            photoData.put("id", p.getId());
            photoData.put("filePath", base64Image);
            photoData.put("tags", p.getTags());
            photoData.put("createdAt", p.getCreatedAt());
            photoData.put("updatedAt", System.currentTimeMillis());

            userPhotosRef.document(p.getId()).set(photoData)
                    .addOnSuccessListener(aVoid -> {
                        if (photos.indexOf(p) == photos.size() - 1) {
                            Toast.makeText(requireContext(), "Upload complete!", Toast.LENGTH_SHORT).show();
                            requireActivity().onBackPressed();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("TakePhotoFragment", "Failed to upload to Firestore", e);
                        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.recyclerViewCapturedPhotos.setAdapter(null);
            binding = null;
        }
    }

}