package com.example.memorai.presentation.viewmodel;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.dynamicanimation.animation.FloatValueHolder;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PhotoViewModel extends ViewModel {
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final Map<String, MutableLiveData<List<Photo>>> albumPhotosMap = new HashMap<>();

    private final MutableLiveData<List<Photo>> searchResults = new MutableLiveData<>();

    private final MutableLiveData<List<Photo>> allPhotos = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final PhotoRepository photoRepository;

    public static final String ROOT_ALBUM_ID = "1";

    @Inject
    public PhotoViewModel(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public LiveData<List<Photo>> observeAllPhotos() {
        return allPhotos;
    }

    public void loadAllPhotos(String userId) {
        CollectionReference userPhotosRef = firestore.collection("photos")
                .document(userId)
                .collection("user_photos");

        userPhotosRef.orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((queryDocumentSnapshots, error) -> {
                    if (error != null) {
                        Log.e("PhotoViewModel", "Error loading photos", error);
                        return;
                    }

                    if (queryDocumentSnapshots == null) {
                        Log.w("PhotoViewModel", "QueryDocumentSnapshots is null");
                        return;
                    }

                    List<Photo> photos = new ArrayList<>();
                    ExecutorService executorService = Executors.newFixedThreadPool(4);
                    Handler mainHandler = new Handler(Looper.getMainLooper());

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        Photo photo = doc.toObject(Photo.class);
                        if (photo != null && photo.getFilePath() != null) {
                            photos.add(photo);

                            // Xử lý ảnh trong thread pool nhưng cập nhật ảnh ngay sau khi xử lý xong
                            executorService.execute(() -> {
                                createBitmap(photo);

                                // Cập nhật ảnh ngay khi xử lý xong thay vì đợi tất cả ảnh
                                mainHandler.post(() -> allPhotos.setValue(new ArrayList<>(photos)));
                            });
                        } else {
                            Log.w("PhotoViewModel", "Photo object or file path is null for document: " + doc.getId());
                        }
                    }

                    // Cập nhật danh sách ảnh ngay lập tức mà không cần chờ giải mã ảnh
                    new Handler(Looper.getMainLooper()).post(() -> allPhotos.setValue(photos));

                    executorService.shutdown();
                });
    }

    public String encodeBase64(String rawString) {
        if (rawString == null || rawString.isEmpty()) {
            return rawString;
        }
        byte[] encodedBytes = Base64.encode(rawString.getBytes(), Base64.DEFAULT);
        return new String(encodedBytes);
    }

    public void loadPhotosByAlbum(String albumId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.w("PhotoViewModel", "User not authenticated");
            return;
        }
        if (!albumPhotosMap.containsKey(albumId)) {
            albumPhotosMap.put(albumId, new MutableLiveData<>());
        }
        MutableLiveData<List<Photo>> result = albumPhotosMap.get(albumId);

        // Tham chiếu đến document album cụ thể
        DocumentReference albumRef = firestore.collection("photos")
                .document(userId)
                .collection("user_albums")
                .document(albumId);

        albumRef.get().addOnCompleteListener(albumTask -> {
            if (!albumTask.isSuccessful() || albumTask.getResult() == null) {
                Log.e("PhotoViewModel", "Error loading album", albumTask.getException());
                return;
            }

            DocumentSnapshot albumDoc = albumTask.getResult();
            List<String> photoIds = (List<String>) albumDoc.get("photos");
            if (photoIds == null || photoIds.isEmpty()) {
                result.postValue(Collections.emptyList());
                return;
            }

            // Lấy tất cả ảnh thuộc album
            CollectionReference photosRef = firestore.collection("photos")
                    .document(userId)
                    .collection("user_photos");

            photosRef.whereIn(FieldPath.documentId(), photoIds)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(photosTask -> {
                        if (!photosTask.isSuccessful()) {
                            Log.e("PhotoViewModel", "Error loading photos", photosTask.getException());
                            return;
                        }

                        QuerySnapshot photosSnapshot = photosTask.getResult();
                        if (photosSnapshot == null || photosSnapshot.isEmpty()) {
                            result.postValue(Collections.emptyList());
                            return;
                        }

                        List<Photo> photos = new ArrayList<>();
                        AtomicInteger processedCount = new AtomicInteger(0);
                        int totalCount = photosSnapshot.size();

                        for (DocumentSnapshot doc : photosSnapshot.getDocuments()) {
                            Photo photo = doc.toObject(Photo.class);
                            if (photo != null) {
                                photos.add(photo);

                                // Xử lý ảnh trong background
                                executorService.execute(() -> {
                                    try {
                                        createBitmap(photo);
                                    } catch (Exception e) {
                                        Log.e("PhotoViewModel", "Error processing image", e);
                                    }

                                    // Cập nhật khi tất cả ảnh đã xử lý xong
                                    if (processedCount.incrementAndGet() == totalCount) {
                                        mainHandler.post(() -> result.setValue(photos));
                                    }
                                });
                            } else {
                                processedCount.incrementAndGet();
                            }
                        }

                        // Xử lý trường hợp tất cả ảnh đã được xử lý ngay lập tức
                        if (processedCount.get() == totalCount) {
                            result.postValue(photos);
                        }
                    });
        });
    }

    public void addPhoto(Photo photo) {
        List<Photo> currentPhotos = allPhotos.getValue();
        if (currentPhotos == null) {
            currentPhotos = new ArrayList<>();
        }

        // Kiểm tra xem ảnh đã tồn tại chưa (dựa vào ID)
        boolean exists = false;
        for (Photo p : currentPhotos) {
            if (p.getId().equals(photo.getId())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            currentPhotos.add(photo);
            allPhotos.setValue(currentPhotos);
        }
    }

    public void deletePhoto(String photoId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.w("PhotoViewModel", "User not authenticated");
            return;
        }

        // Tham chiếu đến ảnh trong Firestore
        DocumentReference photoRef = firestore.collection("photos")
                .document(userId)
                .collection("user_photos")
                .document(photoId);

        // Xóa ảnh trong Firestore
        photoRef.delete().addOnSuccessListener(aVoid -> {
            Log.d("PhotoViewModel", "Photo deleted from Firestore");

            // Tiếp theo, tìm tất cả album chứa ảnh này
            CollectionReference albumsRef = firestore.collection("photos")
                    .document(userId)
                    .collection("user_albums");

            albumsRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                for (DocumentSnapshot albumDoc : queryDocumentSnapshots.getDocuments()) {
                    List<String> photoIds = (List<String>) albumDoc.get("photos");
                    if (photoIds != null && photoIds.contains(photoId)) {
                        // Xóa ID ảnh khỏi danh sách album
                        photoIds.remove(photoId);

                        // Cập nhật album trong Firestore
                        albumDoc.getReference().update("photos", photoIds)
                                .addOnSuccessListener(aVoid1 -> Log.d("PhotoViewModel",
                                        "Photo ID removed from album: " + albumDoc.getId()))
                                .addOnFailureListener(e -> Log.e("PhotoViewModel", "Failed to update album", e));
                    }
                }
            }).addOnFailureListener(e -> Log.e("PhotoViewModel", "Failed to fetch albums", e));
        }).addOnFailureListener(e -> Log.e("PhotoViewModel", "Failed to delete photo", e));

        // Cập nhật LiveData trong ViewModel
        List<Photo> currentPhotos = allPhotos.getValue();
        if (currentPhotos != null) {
            List<Photo> updatedPhotos = new ArrayList<>();
            for (Photo p : currentPhotos) {
                if (!p.getId().equals(photoId)) {
                    updatedPhotos.add(p);
                }
            }
            allPhotos.setValue(updatedPhotos);
        }

        // Xóa ảnh khỏi tất cả album trong ViewModel
        for (MutableLiveData<List<Photo>> albumLiveData : albumPhotosMap.values()) {
            List<Photo> albumPhotos = albumLiveData.getValue();
            if (albumPhotos != null) {
                albumPhotos.removeIf(photo -> photo.getId().equals(photoId));
                albumLiveData.setValue(new ArrayList<>(albumPhotos));
            }
        }
    }

    public void createBitmap(Photo photo) {
        Bitmap bitmap = decodeBase64ToImage(photo.getFilePath());
        photo.setBitmap(bitmap);
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

    public LiveData<List<Photo>> observePhotosByAlbum(String albumId) {
        // Kiểm tra xem album đã có trong map chưa, nếu chưa thì tạo mới
        if (!albumPhotosMap.containsKey(albumId)) {
            albumPhotosMap.put(albumId, new MutableLiveData<>());
            // Tự động load dữ liệu khi lần đầu observe
            loadPhotosByAlbum(albumId);
        }
        return albumPhotosMap.get(albumId);
    }

    public int getPhotoCount() {
        return allPhotos.getValue() != null ? allPhotos.getValue().size() : 0;
    }

    public void searchPhotos(String query) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CollectionReference userPhotosRef = firestore.collection("photos")
                .document(userId)
                .collection("user_photos");

        userPhotosRef.whereGreaterThanOrEqualTo("filePath", query)
                .whereLessThanOrEqualTo("filePath", query + "\uf8ff")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Photo> photos = new ArrayList<>();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Photo photo = doc.toObject(Photo.class);
                            if (photo != null) {
                                photos.add(photo);
                            }
                        }
                        searchResults.setValue(photos);
                    } else {
                        Log.e("PhotoViewModel", "Error searching photos", task.getException());
                    }
                });
    }

    public void clearSearch() {
        searchResults.setValue(new ArrayList<>()); // Đặt danh sách tìm kiếm thành rỗng
    }

    public void setPhotoPrivacy(String photoId, boolean isPrivate) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.w("PhotoViewModel", "User not authenticated");
            return;
        }

        DocumentReference photoRef = firestore.collection("photos")
                .document(userId)
                .collection("user_photos")
                .document(photoId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("private", isPrivate);

        photoRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    List<Photo> currentPhotos = allPhotos.getValue();
                    if (currentPhotos != null) {
                        for (Photo photo : currentPhotos) {
                            if (photo.getId().equals(photoId)) {
                                photo.seIsPrivate(isPrivate);
                                break;
                            }
                        }
                        allPhotos.setValue(new ArrayList<>(currentPhotos));
                    }
                })
                .addOnFailureListener(e -> Log.e("PhotoViewModel", "Failed to update photo privacy", e));
    }

    public LiveData<Photo> getPhotoById(String photoId) {
        MutableLiveData<Photo> photoLiveData = new MutableLiveData<>();

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.w("PhotoViewModel", "User not authenticated");
            return photoLiveData;
        }

        firestore.collection("photos")
                .document(userId)
                .collection("user_photos")
                .document(photoId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Photo photo = documentSnapshot.toObject(Photo.class);
                        if (photo != null) {
                            createBitmap(photo); // Convert base64 to bitmap
                            photoLiveData.setValue(photo);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("PhotoViewModel", "Error fetching photo", e);
                });

        return photoLiveData;
    }

    public LiveData<List<Photo>> getSearchResults() {
        return searchResults;
    }

    public void searchPhotosByTag(String tag) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (userId == null) {
            Log.w("PhotoViewModel", "User not authenticated");
            searchResults.setValue(new ArrayList<>());
            return;
        }

        CollectionReference userPhotosRef = firestore.collection("photos")
                .document(userId)
                .collection("user_photos");

        // Firestore query to find documents where tags array contains the search tag
        userPhotosRef.whereArrayContains("tags", tag)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        List<Photo> photos = new ArrayList<>();

                        // Process photos in background
                        ExecutorService executor = Executors.newFixedThreadPool(4);
                        AtomicInteger processedCount = new AtomicInteger(0);
                        int totalCount = task.getResult().size();

                        if (totalCount == 0) {
                            searchResults.setValue(new ArrayList<>());
                            return;
                        }

                        for (DocumentSnapshot doc : task.getResult()) {
                            Photo photo = doc.toObject(Photo.class);
                            if (photo != null) {
                                photos.add(photo);

                                // Process image in background
                                executor.execute(() -> {
                                    try {
                                        createBitmap(photo);
                                    } catch (Exception e) {
                                        Log.e("PhotoViewModel", "Error processing image", e);
                                    }

                                    // Update UI when all photos are processed
                                    if (processedCount.incrementAndGet() == totalCount) {
                                        mainHandler.post(() -> searchResults.setValue(photos));
                                    }
                                });
                            } else {
                                processedCount.incrementAndGet();
                            }
                        }

                        // Handle case where all photos are processed immediately
                        if (processedCount.get() == totalCount) {
                            searchResults.setValue(photos);
                        }

                        executor.shutdown();
                    } else {
                        Log.e("PhotoViewModel", "Error searching photos by tag", task.getException());
                        searchResults.setValue(new ArrayList<>());
                    }
                });
    }
}
