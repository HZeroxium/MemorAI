package com.example.memorai.presentation.viewmodel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.data.repository.AlbumRepositoryImpl;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AlbumViewModel extends ViewModel {

    private final AlbumRepositoryImpl albumRepository;
    private final MutableLiveData<List<Album>> albumsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Album> albumLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Photo>> photosLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>();

    @Inject
    public AlbumViewModel(AlbumRepositoryImpl albumRepository) {
        this.albumRepository = albumRepository;
    }

    public void loadAlbums() {
        executorService.execute(() -> {
            try {
                List<Album> albums = albumRepository.getAlbums();
                List<Album> result = new ArrayList<>();

                for (Album album : albums) {
                    Album newAlbum = new Album(
                            album.getId(),
                            album.getName(),
                            album.getDescription(),
                            album.getPhotos(),
                            album.getCoverPhotoUrl(),
                            album.getCreatedAt(),
                            album.getUpdatedAt(),
                            album.isPrivate()
                    );

                    // Xử lý ảnh bìa nếu có URL
                    if (newAlbum.getCoverPhotoUrl() != null && !newAlbum.getCoverPhotoUrl().isEmpty()) {
                        try {
                            Bitmap bitmap = decodeBase64ToImage(newAlbum.getCoverPhotoUrl());
                            if (bitmap != null) {
                                Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                                        bitmap,
                                        200, 200, // Kích thước phù hợp
                                        true
                                );
                                if (bitmap != scaledBitmap) {
                                    bitmap.recycle();
                                }
                                newAlbum.setBitmap(scaledBitmap);
                            }
                        } catch (Exception e) {
                            // Xử lý lỗi nếu không decode được ảnh
                            Log.e("AlbumViewModel", "Error decoding cover photo", e);
                        }
                    }
                    // Nếu không có ảnh bìa, vẫn thêm album vào kết quả
                    result.add(newAlbum);
                }
                mainHandler.post(() -> albumsLiveData.setValue(result));
            } catch (Exception e) {
                mainHandler.post(() -> errorLiveData.setValue("Failed to load albums: " + e.getMessage()));
            }
        });
    }

    public LiveData<List<Album>> getAlbums() {
        return albumsLiveData;
    }

    public int getAlbumCount() {
        return albumsLiveData.getValue() != null ? albumsLiveData.getValue().size() : 0;
    }

    public void loadAlbumById(String albumId) {
        executorService.execute(() -> {
            mainHandler.post(() -> albumLiveData.setValue(null));
            Album album = albumRepository.getAlbumById(albumId);
            if (album != null && album.getCoverPhotoUrl() != null) {
                Bitmap bitmap = decodeBase64ToImage(album.getCoverPhotoUrl());
                album.setBitmap(bitmap);
            }
            // Clear albumlivedata truoc
            mainHandler.post(() -> albumLiveData.setValue(album));
        });
    }

    public LiveData<Album> getAlbumLiveData() {
        return albumLiveData;
    }

    public void loadPhotosFromAlbum(String albumId) {
        executorService.execute(() -> {
            Album album = albumRepository.getAlbumById(albumId);
            if (album != null && album.getPhotos() != null) {
                List<Photo> photos = fetchPhotosFromLocal(album.getPhotos());
                mainHandler.post(() -> photosLiveData.setValue(photos));
            } else {
                mainHandler.post(() -> photosLiveData.setValue(new ArrayList<>()));
            }
        });
    }

    public LiveData<List<Photo>> getPhotosLiveData() {
        return photosLiveData;
    }

    public void addAlbum(Album album) {
        executorService.execute(() -> {
            albumRepository.addAlbum(album);
            loadAlbums();
        });
    }

    public void updateAlbum(Album album) {
        executorService.execute(() -> {
            albumRepository.updateAlbum(album);
            loadAlbums();
        });
    }

    public void deleteAlbum(String albumId) {
        executorService.execute(() -> {
            albumRepository.deleteAlbum(albumId);
            loadAlbums();
        });
    }

    public void createAlbumWithPhotos(Album album, List<Photo> photos) {
        executorService.execute(() -> {
            albumRepository.createAlbumWithPhotos(album, photos);
            loadAlbums();
        });
    }

    public void updateAlbumWithPhotos(Album album, List<Photo> photos) {
        executorService.execute(() -> {
            albumRepository.updateAlbumWithPhotos(album, photos);
            loadAlbums();
        });
    }


    public void syncAllPendingChanges(SyncCallback callback) {
        callback.onSyncStarted();

        executorService.execute(() -> {
            try {
                albumRepository.syncPendingChangesToFirebase();

                albumRepository.syncFromFirebaseAsync().thenRun(() -> {
                    List<Album> albums = albumRepository.getAlbums();
                    List<Album> result = new ArrayList<>();

                    for (Album album : albums) {
                        Album newAlbum = new Album(album.getId(), album.getName(),
                                album.getDescription(), album.getPhotos(),
                                album.getCoverPhotoUrl(), album.getCreatedAt(),
                                album.getUpdatedAt(), album.isPrivate());

                        if (newAlbum.getCoverPhotoUrl() != null) {
                            Bitmap bitmap = decodeBase64ToImage(newAlbum.getCoverPhotoUrl());
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
                            if (bitmap != scaledBitmap) {
                                bitmap.recycle();
                            }
                            newAlbum.setBitmap(scaledBitmap);
                        }
                        result.add(newAlbum);
                    }

                    mainHandler.post(() -> {
                        albumsLiveData.setValue(result);
                        callback.onSyncCompleted(true);
                    });
                }).exceptionally(e -> {
                    mainHandler.post(() -> {
                        errorLiveData.setValue("Sync failed: " + e.getMessage());
                        callback.onSyncCompleted(false);
                    });
                    return null;
                });
            } catch (Exception e) {
                mainHandler.post(() -> {
                    errorLiveData.setValue("Sync failed: " + e.getMessage());
                    callback.onSyncCompleted(false);
                });
            }
        });
    }

    public void clearAlbum() {
        albumLiveData.setValue(null);
    }

    public interface SyncCallback {
        void onSyncStarted();
        void onSyncCompleted(boolean isSuccess);
    }

    public static Bitmap decodeBase64ToImage(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("Photo", "Error decoding Base64 to image", e);
            return null;
        }
    }

    private List<Photo> fetchPhotosFromLocal(List<String> photoIds) {
        // TODO: Triển khai PhotoDao để lấy Photo từ Room
        return new ArrayList<>();
    }

    private String getUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}