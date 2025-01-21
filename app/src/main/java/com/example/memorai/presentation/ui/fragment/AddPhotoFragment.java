// presentation/ui/fragment/AddPhotoFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

public class AddPhotoFragment extends Fragment {

    private static final int REQUEST_CAMERA_CODE = 100;    // Code for camera intent
    private static final int REQUEST_GALLERY_CODE = 101;   // Code for gallery intent

    private static final int PERMISSION_CAMERA = 200;
    private static final int PERMISSION_READ_STORAGE = 201;

    private Uri photoUri;             // Lưu URI ảnh từ camera
    private ImageView imageViewPreview; // Hiển thị ảnh ngay sau khi chụp/chọn

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_photo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        imageViewPreview = view.findViewById(R.id.imageViewPreview);

        // Xử lý nút Open Camera
        view.findViewById(R.id.btnOpenCamera).setOnClickListener(v -> openCamera());

        // Xử lý nút Open Gallery
        view.findViewById(R.id.btnOpenGallery).setOnClickListener(v -> openGallery());
    }

    private void openCamera() {
        // Kiểm tra quyền CAMERA
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền
            launchCameraIntent();
        } else {
            // Chưa có quyền -> yêu cầu
            requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CAMERA);
        }
    }

    private void openGallery() {
        // Kiểm tra quyền READ_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền
            launchGalleryIntent();
        } else {
            // Chưa có quyền -> yêu cầu
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_READ_STORAGE);
        }
    }

    /**
     * Tạo Intent chụp ảnh, lưu ảnh vào MediaStore.
     */
    private void launchCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Tạo URI để camera lưu ảnh
        photoUri = createImageUri();
        if (photoUri != null) {
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(cameraIntent, REQUEST_CAMERA_CODE);
        } else {
            Toast.makeText(requireContext(), "Failed to create image Uri", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Tạo Intent chọn ảnh từ Gallery.
     */
    private void launchGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Hoặc dùng ACTION_OPEN_DOCUMENT nếu muốn Android 11+ an toàn hơn
        startActivityForResult(galleryIntent, REQUEST_GALLERY_CODE);
    }

    /**
     * Tạo URI ảnh trong MediaStore (public). Hoặc có thể tạo file trong private storage + FileProvider.
     */
    private Uri createImageUri() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "Captured_Img_" + System.currentTimeMillis());
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        return requireContext().getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    /**
     * Xử lý kết quả yêu cầu quyền
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CAMERA) {
            // CAMERA
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchCameraIntent();
            } else {
                Toast.makeText(requireContext(), "Camera Permission Denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSION_READ_STORAGE) {
            // READ_EXTERNAL_STORAGE
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                launchGalleryIntent();
            } else {
                Toast.makeText(requireContext(), "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Xử lý kết quả trả về từ Camera/Gallery
     */
    @Override
    public void onActivityResult(int requestCode,
                                 int resultCode,
                                 Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA_CODE:
                    handleCameraResult(photoUri);
                    break;
                case REQUEST_GALLERY_CODE:
                    if (data != null && data.getData() != null) {
                        handleGalleryResult(data.getData());
                    }
                    break;
            }
        }
    }

    private void handleCameraResult(Uri cameraUri) {
        // Hiển thị ảnh
        if (cameraUri != null) {
            Glide.with(this).load(cameraUri).into(imageViewPreview);
            // TODO: Ở bước sau, lưu cameraUri vào Database hoặc upload Firebase, v.v.

            // Lưu photo vào Room
            // (VD: albumId = 1, tạm, hoặc real albumId user chọn)
            PhotoViewModel photoViewModel = new ViewModelProvider(this)
                    .get(PhotoViewModel.class);

            // Chẳng hạn albumId = 1
            int albumId = 1;
            photoViewModel.addPhoto(cameraUri.toString(), albumId);
        }
    }

    private void handleGalleryResult(Uri galleryUri) {
        Glide.with(this).load(galleryUri).into(imageViewPreview);
        // TODO: Lưu galleryUri vào Room Database hoặc list Photo
        PhotoViewModel photoViewModel = new ViewModelProvider(this)
                .get(PhotoViewModel.class);

        int albumId = 1;
        photoViewModel.addPhoto(galleryUri.toString(), albumId);
    }
}
