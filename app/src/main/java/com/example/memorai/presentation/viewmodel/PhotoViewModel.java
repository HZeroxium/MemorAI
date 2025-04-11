package com.example.memorai.presentation.viewmodel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.data.repository.PhotoRepositoryImpl;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import dagger.hilt.android.qualifiers.ApplicationContext;

@HiltViewModel
public class PhotoViewModel extends ViewModel {
    private final PhotoRepository photoRepository;
    private final Context context;

    private final MutableLiveData<String> toastEvent = new MutableLiveData<>();
    private final MutableLiveData<List<Photo>> allPhotos = new MutableLiveData<>();

    private final MutableLiveData<List<Photo>> albumPhotos = new MutableLiveData<>();
    private final MutableLiveData<List<Photo>> searchResults = new MutableLiveData<>();
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private MutableLiveData<Boolean> _isSyncing = new MutableLiveData<>(false);
    public LiveData<Boolean> isSyncing() { return _isSyncing; }

    @Inject
    public PhotoViewModel(PhotoRepository photoRepository, @ApplicationContext Context context) {
        this.photoRepository = photoRepository;
        this.context = context.getApplicationContext();
        loadAllPhotos();
    }

    public LiveData<List<Photo>> observeAllPhotos() {
        return allPhotos;
    }

    public LiveData<List<Photo>> observePhotosByAlbum() {
        return albumPhotos;
    }

    public LiveData<String> getToastEvent() {
        return toastEvent;
    }

    public LiveData<List<Photo>> getSearchResults() {
        return searchResults;
    }

    public void loadAllPhotos() {
        executorService.execute(() -> {
            List<Photo> photos = photoRepository.getAllPhotos().stream()
                    .filter(photo -> !photo.isPrivate())
                    .collect(Collectors.toList());
            for (Photo photo : photos) {
                photo.setBitmap(decodeBase64ToImage(photo.getFilePath()));
            }
            allPhotos.postValue(photos);
        });
    }

    public void loadPhotosByAlbum(String albumId) {
        executorService.execute(() -> {
            new Handler(Looper.getMainLooper()).post(() -> {
                albumPhotos.setValue(new ArrayList<>());
            });

            List<Photo> photos = photoRepository.getPhotosByAlbum(albumId);
            for (Photo photo : photos) {
                photo.setBitmap(decodeBase64ToImage(photo.getFilePath()));
            }
            albumPhotos.postValue(photos);
        });
    }

    public int getPhotoCount() {
        List<Photo> photos = allPhotos.getValue();
        return photos != null ? photos.size() : 0;
    }

    public void searchPhotosByTag(String query, Context context) {
        if (query.isEmpty()) {
            Toast.makeText(context, "Enter a tag to search", Toast.LENGTH_SHORT).show();
            searchResults.postValue(new ArrayList<>()); // Clear previous results
            return;
        }

        executorService.execute(() -> {
            try {
                List<Photo> allPhotosList = photoRepository.getAllPhotos();
                List<Photo> matchedPhotos = new ArrayList<>();

                // Lọc ảnh có chứa tag trùng khớp (không phân biệt hoa thường)
                for (Photo photo : allPhotosList) {
                    if (photo.getTags() != null) {
                        for (String tag : photo.getTags()) {
                            if (tag.toLowerCase().contains(query.toLowerCase())) {
                                matchedPhotos.add(photo);
                                break; // Thêm ảnh 1 lần nếu có ít nhất 1 tag khớp
                            }
                        }
                    }
                }
                for (Photo photo : matchedPhotos) {
                    photo.setBitmap(decodeBase64ToImage(photo.getFilePath()));
                }
                // Cập nhật kết quả lên LiveData
                searchResults.postValue(matchedPhotos);

                // Thông báo nếu không tìm thấy kết quả
                if (matchedPhotos.isEmpty()) {
                    mainHandler.post(() ->
                            Toast.makeText(context, "No photos found with tag: " + query, Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                Log.e("PhotoViewModel", "Error searching photos by tag", e);
                mainHandler.post(() ->
                        Toast.makeText(context, "Error searching photos", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    public void addPhoto(Bitmap bitmap, List<String> tags) {
        executorService.execute(() -> {
            try {
                String base64Image = convertBitmapToBase64(bitmap);
                String photoId = UUID.randomUUID().toString();
                Photo photo = new Photo(photoId, base64Image, tags, System.currentTimeMillis(), System.currentTimeMillis());
                photoRepository.addPhoto(photo);
                updateLocalPhotos(photo);
                toastEvent.postValue("Photo added successfully");
            } catch (Exception e) {
                Log.e("PhotoViewModel", "Error adding photo", e);
                toastEvent.postValue("Error adding photo");
            }
        });
    }

    public void updatePhoto(Photo photo) {
        photoRepository.updatePhoto(photo);
        updateLocalPhotos(photo);
        toastEvent.postValue("Photo updated successfully");
    }

    public void deletePhoto(String photoId) {
        photoRepository.deletePhoto(photoId);
        List<Photo> currentPhotos = allPhotos.getValue();
        if (currentPhotos != null) {
            currentPhotos.removeIf(photo -> photo.getId().equals(photoId));
            allPhotos.setValue(new ArrayList<>(currentPhotos));
            toastEvent.postValue("Photo deleted successfully");
        }
    }

    public void searchPhotos(String query) {
        executorService.execute(() -> {
            List<Photo> results = photoRepository.searchPhotos(query);
            searchResults.postValue(results);
        });
    }

    public void clearSearch() {
        searchResults.setValue(new ArrayList<>());
    }

    public void clearAlbumPhoto() {
        albumPhotos.setValue(new ArrayList<>());
    }

    public void setPhotoPrivacy(String photoId, boolean isPrivate) {
        photoRepository.setPhotoPrivacy(photoId, isPrivate);
        List<Photo> currentPhotos = allPhotos.getValue();
        if (currentPhotos != null) {
            for (Photo photo : currentPhotos) {
                if (photo.getId().equals(photoId)) {
                    photo.setIsPrivate(isPrivate);
                    break;
                }
            }
            allPhotos.setValue(new ArrayList<>(currentPhotos));
        }

        photoRepository.getPrivateAlbumId(new PhotoRepositoryImpl.OnResultCallback<String>() {
            @Override
            public void onResult(String albumId) {
                // Xử lý kết quả trên main thread
                if (albumId != null) {
                    if (isPrivate) {
                        photoRepository.addPhotoToAlbum(photoId, albumId);
                    } else {
                        photoRepository.removePhotoFromAlbum(photoId, albumId);
                    }
                }
            }
        });
    }

    public LiveData<List<Photo>> observePhotosByAlbum(String albumId, boolean includePrivate) {
        MutableLiveData<List<Photo>> result = new MutableLiveData<>();
        executorService.execute(() -> {
            List<Photo> photos = photoRepository.getPhotosByAlbum(albumId, includePrivate);
            for (Photo photo : photos) {
                photo.setBitmap(decodeBase64ToImage(photo.getFilePath()));
            }
            result.postValue(photos);
        });
        return result;
    }

    public LiveData<Photo> getPhotoById(String photoId) {
        MutableLiveData<Photo> photoLiveData = new MutableLiveData<>();
        executorService.execute(() -> {
            Photo photo = photoRepository.getPhotoById(photoId);
            if (photo != null) {
                photo.setBitmap(decodeBase64ToImage(photo.getFilePath()));
                photoLiveData.postValue(photo);
            }
        });
        return photoLiveData;
    }

    private void updateLocalPhotos(Photo updatedPhoto) {
        List<Photo> currentPhotos = allPhotos.getValue();
        if (currentPhotos == null) {
            currentPhotos = new ArrayList<>();
            currentPhotos.add(updatedPhoto);
        } else {
            boolean found = false;
            for (int i = 0; i < currentPhotos.size(); i++) {
                if (currentPhotos.get(i).getId().equals(updatedPhoto.getId())) {
                    currentPhotos.set(i, updatedPhoto);
                    found = true;
                    break;
                }
            }
            if (!found) {
                updatedPhoto.setBitmap(decodeBase64ToImage(updatedPhoto.getFilePath()));
                currentPhotos.add(updatedPhoto);
            }
        }
        allPhotos.postValue(new ArrayList<>(currentPhotos));
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private Bitmap decodeBase64ToImage(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("PhotoViewModel", "Error decoding Base64 to image", e);
            return null;
        }
    }

    public void syncPhoto(SyncCallback callback) {
        // Hiển thị loading trước khi sync
        callback.onSyncStarted();

        executorService.execute(() -> {
            try {
                // Giả lập thời gian sync (test)
                Thread.sleep(2000);

                // 1. Đồng bộ từ Firestore về trước
                photoRepository.syncFromFirestore();

                // 2. Đồng bộ dữ liệu local lên Firestore
                photoRepository.syncLocalPhotosToFirestore();

                // Thành công
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSyncCompleted(true);
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    callback.onSyncCompleted(false);
                });
            }
        });
    }


    // Interface callback
    public interface SyncCallback {
        void onSyncStarted();
        void onSyncCompleted(boolean isSuccess);
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}