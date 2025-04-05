package com.example.memorai.presentation.viewmodel;

import static androidx.core.content.ContentProviderCompat.requireContext;

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

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlbumViewModel extends ViewModel {

    private final FirebaseFirestore firestore;
    private final MutableLiveData<List<Album>> albumsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Album> albumLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Album>> allAlbumsLiveData = new MutableLiveData<>();
    private ListenerRegistration listenerRegistration;
    private ListenerRegistration allAlbumsListener;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());


    public AlbumViewModel() {
        firestore = FirebaseFirestore.getInstance();
    }

    public void loadAlbums() {
        String userId = getUserId();
        if (userId == null) return;

        CollectionReference userAlbumsRef = firestore.collection("photos")
                .document(userId)
                .collection("user_albums");

        listenerRegistration = userAlbumsRef.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            List<Album> albumList = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Album album = doc.toObject(Album.class);
                String coverPhotoBase64 = doc.getString("coverPhotoUrl");
                executorService.execute(() -> {
                    Bitmap bitmap = decodeBase64ToImage(coverPhotoBase64);
                    album.setBitmap(bitmap);

                    // Cập nhật LiveData trên UI thread ngay khi ảnh được xử lý xong
                    mainHandler.post(() -> {
                        albumList.add(album);
                        albumsLiveData.setValue(new ArrayList<>(albumList));
                    });
                });
            }
            albumsLiveData.postValue(albumList);
        });
    }

    public LiveData<List<Album>> getAlbums() {
        return albumsLiveData;
    }

    public int getAlbumCount() {
        return albumsLiveData.getValue() != null ? albumsLiveData.getValue().size() : 0;
    }

    /**
     * Lấy album cụ thể theo ID.
     */
    public void loadAlbumById(String albumId) {
        String userId = getUserId();
        if (userId == null) return;

        firestore.collection("photos")
                .document(userId)
                .collection("user_albums")
                .document(albumId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Album album = documentSnapshot.toObject(Album.class);
                        albumLiveData.postValue(album);
                    } else {
                        albumLiveData.postValue(null);
                    }
                })
                .addOnFailureListener(e -> {
                    albumLiveData.postValue(null);
                });
    }

    /**
     * LiveData quan sát album cụ thể.
     */
    public LiveData<Album> getAlbumLiveData() {
        return albumLiveData;
    }

    private final MutableLiveData<List<Photo>> photosLiveData = new MutableLiveData<>();

    public void loadPhotosFromAlbum(String userId, String albumId) {
        firestore.collection("photos")
                .document(userId)
                .collection("user_albums")
                .document(albumId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Album album = documentSnapshot.toObject(Album.class);
                        if (album != null && album.getPhotos() != null) {
                            fetchPhotos(userId, album.getPhotos());
                        }
                    }
                })
                .addOnFailureListener(e -> photosLiveData.postValue(new ArrayList<>()));
    }

    public void loadAllAlbums(String userId) {
        if (userId == null) return;

        if (allAlbumsLiveData.getValue() != null && !allAlbumsLiveData.getValue().isEmpty()) {
            return;
        }

        CollectionReference userAlbumsRef = firestore.collection("photos")
                .document(userId)
                .collection("user_albums");

        allAlbumsListener = userAlbumsRef.addSnapshotListener((snapshots, e) -> {
            if (e != null || snapshots == null) return;

            List<Album> albumList = new ArrayList<>();
            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                Album album = doc.toObject(Album.class);
                String coverPhotoBase64 = doc.getString("coverPhotoUrl");
                Bitmap bitmap = decodeBase64ToImage(coverPhotoBase64);
                album.setBitmap(bitmap);

                if (album != null) {
                    albumList.add(album);
                }
            }
            allAlbumsLiveData.postValue(albumList); // Cập nhật LiveData
        });
    }

    public Bitmap getCoverPhotoBitmap(String coverPhotoUrl) {
        return this.decodeBase64ToImage(coverPhotoUrl);
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

    public void updateAlbumWithPhotos(String userId, String albumId, List<String> newPhotoIds) {
        if (userId == null || albumId == null) return;

        firestore.collection("photos")
                .document(userId)
                .collection("user_albums")
                .document(albumId)
                .update("photos", newPhotoIds, "updatedAt", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d("AlbumViewModel", "Album updated successfully with new photos.");
                })
                .addOnFailureListener(e -> {
                    Log.e("AlbumViewModel", "Error updating album", e);
                });
    }


    // Truy vấn ảnh từ danh sách photoIds trong user_photos
    private void fetchPhotos(String userId, List<String> photoIds) {
        List<Photo> photoList = new ArrayList<>();
        for (String photoId : photoIds) {
            firestore.collection("photos")
                    .document(userId)
                    .collection("user_photos")
                    .document(photoId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        Photo photo = documentSnapshot.toObject(Photo.class);
                        if (photo != null) {
                            photoList.add(photo);
                            photosLiveData.postValue(photoList);
                        }
                    })
                    .addOnFailureListener(e -> {
                        // Xử lý lỗi nếu cần
                    });
        }
    }



    public LiveData<List<Photo>> getPhotosLiveData() {
        return photosLiveData;
    }

    /**
     * LiveData để quan sát tất cả album.
     */
    public LiveData<List<Album>> getAllAlbumsLiveData() {
        return allAlbumsLiveData;
    }

    private String getUserId() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        return (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : null;
    }

    public void deleteAlbum(String userId, String albumId) {
        firestore.collection("photos")
                .document(userId)
                .collection("user_albums")
                .document(albumId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("AlbumViewModel", "Album deleted successfully.");
                })
                .addOnFailureListener(e -> {
                    Log.e("AlbumViewModel", "Error deleting album", e);
                });
    }

    public LiveData<Album> getAlbumById(String albumId) {
        loadAlbumById(albumId); // Gọi hàm tải album
        return albumLiveData; // Trả về LiveData quan sát album
    }



    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
        if (allAlbumsListener != null) {
            allAlbumsListener.remove();
        }
    }
}
