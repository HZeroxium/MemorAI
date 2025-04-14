package com.example.memorai.data.repository;

import android.util.Log;

import com.example.memorai.data.local.dao.PhotoAlbumCrossRefDao;
import com.example.memorai.data.local.dao.PhotoDao;
import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.entity.PhotoAlbumCrossRef;
import com.example.memorai.data.local.entity.PhotoEntity;
import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.data.mappers.PhotoMapper;
import com.example.memorai.data.mappers.AlbumMapper;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.PhotoRepository;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PhotoRepositoryImpl implements PhotoRepository {

    private final PhotoDao photoDao;
    private final PhotoAlbumCrossRefDao crossRefDao;
    private final AlbumDao albumDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Inject
    public PhotoRepositoryImpl(PhotoDao photoDao,
                               PhotoAlbumCrossRefDao crossRefDao,
                               AlbumDao albumDao,
                               FirebaseFirestore firestore,
                               FirebaseAuth firebaseAuth) {
        this.photoDao = photoDao;
        this.crossRefDao = crossRefDao;
        this.albumDao = albumDao;
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
    }

    private String getCurrentUserId() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // --- Photo Methods ---

    @Override
    public void setPhotoPrivacy(String photoId, boolean isPrivate) {
        executorService.execute(() -> {
            // Cập nhật trạng thái private trong Room
            photoDao.updatePhotoPrivacy(photoId, isPrivate);

            String userId = getCurrentUserId();
            if (userId == null) return;

            // Cập nhật trạng thái private trong Firestore
            firestore.collection("photos")
                    .document(userId)
                    .collection("user_photos")
                    .document(photoId)
                    .update("isPrivate", isPrivate)
                    .addOnFailureListener(e -> Log.e("PhotoRepository", "Failed to update photo privacy in Firestore", e));

            // Nếu đặt thành private, kiểm tra và cập nhật ảnh bìa của album
            if (isPrivate) {
                CollectionReference albumsRef = firestore.collection("photos")
                        .document(userId)
                        .collection("user_albums");

                albumsRef.get()
                        .addOnSuccessListener(querySnapshot -> {
                            // Di chuyển logic truy vấn Room vào executorService
                            executorService.execute(() -> {
                                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                    List<String> photoIds = (List<String>) document.get("photos");
                                    String currentCover = document.getString("coverPhotoUrl");
                                    PhotoEntity photoEntity = photoDao.getPhotoById(photoId); // Chạy trên background thread
                                    String photoUrl = photoEntity != null ? photoEntity.filePath : null;

                                    // Kiểm tra xem ảnh có phải là ảnh bìa không
                                    if (photoIds != null && photoUrl != null && currentCover != null && currentCover.equals(photoUrl)) {
                                        // Tìm ảnh không private đầu tiên để làm ảnh bìa mới
                                        String newCoverId = null;
                                        String newCoverUrl = null;
                                        for (String pid : photoIds) {
                                            if (!pid.equals(photoId)) {
                                                PhotoEntity candidate = photoDao.getPhotoById(pid); // Chạy trên background thread
                                                if (candidate != null && !candidate.isPrivate) {
                                                    newCoverId = pid;
                                                    newCoverUrl = candidate.filePath;
                                                    break;
                                                }
                                            }
                                        }

                                        // Tạo Map mới cho updates
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("updatedAt", System.currentTimeMillis());

                                        // Biến final để sử dụng trong lambda
                                        final String finalCoverUrl = newCoverUrl != null ? newCoverUrl : "";

                                        // Cập nhật ảnh bìa
                                        updates.put("coverPhotoUrl", finalCoverUrl);

                                        // Cập nhật Room
                                        AlbumEntity albumEntity = albumDao.getAlbumById(document.getId());
                                        if (albumEntity != null) {
                                            albumEntity.coverPhotoUrl = finalCoverUrl;
                                            albumEntity.isSynced = false;
                                            albumDao.updateAlbum(albumEntity);
                                        }

                                        // Cập nhật Firestore
                                        albumsRef.document(document.getId())
                                                .update(updates)
                                                .addOnSuccessListener(aVoid -> Log.d("PhotoRepository", "Updated cover for album: " + document.getId()))
                                                .addOnFailureListener(e -> Log.e("PhotoRepository", "Failed to update album cover: " + document.getId(), e));
                                    }
                                }
                            });
                        })
                        .addOnFailureListener(e -> Log.e("PhotoRepository", "Failed to fetch albums for cover update", e));
            }
        });
    }

    @Override
    public Photo getPhotoById(String photoId) {
        PhotoEntity entity = photoDao.getPhotoById(photoId);
        return entity != null ? PhotoMapper.toDomain(entity) : null;
    }

    @Override
    public List<Photo> getAllPhotos() {
        return photoDao.getAllPhotos().stream()
                .map(PhotoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void addPhoto(Photo photo) {
        executorService.execute(() -> {
            PhotoEntity entity = PhotoMapper.fromDomain(photo);
            entity.isSynced = false;
            photoDao.insertPhoto(entity);

            String userId = getCurrentUserId();
            if (userId != null) {
                Map<String, Object> photoData = new HashMap<>();
                photoData.put("id", photo.getId());
                photoData.put("filePath", photo.getFilePath());
                photoData.put("isPrivate", false);
                photoData.put("tags", photo.getTags());
                photoData.put("createdAt", photo.getCreatedAt());
                photoData.put("updatedAt", System.currentTimeMillis());

                firestore.collection("photos")
                        .document(userId)
                        .collection("user_photos")
                        .document(photo.getId())
                        .set(photoData)
                        .addOnSuccessListener(aVoid -> {
                            executorService.execute(() -> {
                                entity.isSynced = true;
                                photoDao.updatePhoto(entity);
                            });
                        })
                        .addOnFailureListener(e ->
                                Log.e("PhotoRepository", "Firestore sync failed", e));
            }
        });
    }

    @Override
    public void updatePhoto(Photo photo) {
        executorService.execute(() -> {
            PhotoEntity entity = PhotoMapper.fromDomain(photo);
            entity.isSynced = false;
            photoDao.updatePhoto(entity);

            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_photos")
                        .document(photo.getId())
                        .set(photo)
                        .addOnSuccessListener(aVoid -> {
                            entity.isSynced = true;
                            photoDao.updatePhoto(entity);
                        });
            }
        });
    }

    @Override
    public void deletePhoto(String photoId) {
        executorService.execute(() -> {
            // Xóa photo từ Room
            PhotoEntity entity = photoDao.getPhotoById(photoId);
            String photoUrl = entity.filePath;
            if (entity != null) {
                photoDao.deletePhoto(entity);
            }
            crossRefDao.deleteCrossRefsForPhoto(photoId);

            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_photos")
                        .document(photoId)
                        .delete()
                        .addOnFailureListener(e -> Log.e("PhotoRepository", "Failed to delete photo from Firestore", e));

                CollectionReference albumsRef = firestore.collection("photos")
                        .document(userId)
                        .collection("user_albums");

                albumsRef.get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                List<String> photoIds = (List<String>) document.get("photos");
                                if (photoIds != null && photoIds.contains(photoId)) {
                                    photoIds.remove(photoId);
                                    boolean needUpdateCover = false;
                                    String currentCover = document.getString("coverPhotoUrl");
                                    if (currentCover != null && currentCover.equals(photoUrl)) {
                                        needUpdateCover = true;
                                    }

                                    Map<String, Object> updates = new HashMap<>();
                                    updates.put("photos", photoIds);
                                    updates.put("updatedAt", System.currentTimeMillis());

                                    if (needUpdateCover && !photoIds.isEmpty()) {
                                        executorService.execute(() -> {
                                            String newCoverId = photoIds.get(0);
                                            PhotoEntity newCoverEntity = photoDao.getPhotoById(newCoverId);
                                            if (newCoverEntity != null) {
                                                updates.put("coverPhotoUrl", newCoverEntity.filePath);
                                                executorService.execute(() -> {
                                                    AlbumEntity albumEntity = albumDao.getAlbumById(document.getId());
                                                    if (albumEntity != null) {
                                                        albumEntity.coverPhotoUrl = newCoverEntity.filePath;
                                                        albumDao.updateAlbum(albumEntity);
                                                    }
                                                });
                                            }

                                            albumsRef.document(document.getId())
                                                    .update(updates)
                                                    .addOnSuccessListener(aVoid -> Log.d("AlbumRepository", "Removed photoId and updated cover: " + document.getId()))
                                                    .addOnFailureListener(e -> Log.e("AlbumRepository", "Failed to update album: " + document.getId(), e));
                                        });
                                    } else {
                                        if (needUpdateCover) {
                                            updates.put("coverPhotoUrl", "");
                                            executorService.execute(() -> {
                                                AlbumEntity albumEntity = albumDao.getAlbumById(document.getId());
                                                if (albumEntity != null) {
                                                    albumEntity.coverPhotoUrl = "";
                                                    albumDao.updateAlbum(albumEntity);
                                                }
                                            });
                                        }

                                        albumsRef.document(document.getId())
                                                .update(updates)
                                                .addOnSuccessListener(aVoid -> Log.d("AlbumRepository", "Removed photoId from album: " + document.getId()))
                                                .addOnFailureListener(e -> Log.e("AlbumRepository", "Failed to update album: " + document.getId(), e));
                                    }
                                }
                            }
                        })
                        .addOnFailureListener(e -> Log.e("AlbumRepository", "Failed to fetch albums for photo deletion", e));
            }
        });
    }

    @Override
    public List<Photo> searchPhotos(String query) {
        return photoDao.searchPhotos(query).stream()
                .map(PhotoMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Photo> getPhotosByAlbum(String albumId) {
        List<PhotoAlbumCrossRef> crossRefs = crossRefDao.getCrossRefsForAlbum(albumId);
        return crossRefs.stream()
                .map(crossRef -> getPhotoById(crossRef.getPhotoId()))
                .filter(photo -> photo != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<Photo> getPhotosSorted(String albumId, String sortBy) {
        List<Photo> photos = getPhotosByAlbum(albumId);
        switch (sortBy.toLowerCase()) {
            case "createdat":
                photos.sort((p1, p2) -> Long.compare(p2.getCreatedAt(), p1.getCreatedAt()));
                break;
            case "updatedat":
                photos.sort((p1, p2) -> Long.compare(p2.getUpdatedAt(), p1.getUpdatedAt()));
                break;
            default:
                throw new UnsupportedOperationException("Sort by " + sortBy + " is not supported.");
        }
        return photos;
    }

    @Override
    public List<Photo> getPhotosByAlbum(String albumId, boolean includePrivate) {
        List<PhotoAlbumCrossRef> crossRefs = crossRefDao.getCrossRefsForAlbum(albumId);
        return crossRefs.stream()
                .map(crossRef -> {
                    PhotoEntity entity = photoDao.getPhotoById(crossRef.getPhotoId());
                    if (entity != null && (includePrivate || !entity.isPrivate)) {
                        return PhotoMapper.toDomain(entity);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // --- Album Methods ---

    @Override
    public void addAlbum(Album album) {
        executorService.execute(() -> {
            AlbumEntity entity = AlbumMapper.fromDomain(album);
            entity.isSynced = false;
            albumDao.insertAlbum(entity);

            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_albums")
                        .document(album.getId())
                        .set(album)
                        .addOnSuccessListener(aVoid -> {
                            entity.isSynced = true;
                            albumDao.updateAlbum(entity);
                        });
            }
        });
    }

    @Override
    public void deleteAllPhotos() {
        executorService.execute(() -> {
            photoDao.deleteAllPhotos();
            crossRefDao.deleteAllCrossRefs();

            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_photos")
                        .get()
                        .addOnSuccessListener(querySnapshot -> {
                            for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                doc.getReference().delete();
                            }
                        });
            }
        });
    }

    @Override
    public void updateAlbum(Album album) {
        executorService.execute(() -> {
            AlbumEntity entity = AlbumMapper.fromDomain(album);
            entity.isSynced = false;
            albumDao.updateAlbum(entity);

            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_albums")
                        .document(album.getId())
                        .set(album)
                        .addOnSuccessListener(aVoid -> {
                            entity.isSynced = true;
                            albumDao.updateAlbum(entity);
                        });
            }
        });
    }

    @Override
    public void deleteAlbum(String albumId) {
        executorService.execute(() -> {
            albumDao.deleteAlbum(albumId);
            crossRefDao.deleteCrossRefsForAlbum(albumId);

            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_albums")
                        .document(albumId)
                        .delete();
            }
        });
    }

    @Override
    public Album getAlbumById(String albumId) {
        AlbumEntity entity = albumDao.getAlbumById(albumId);
        if (entity == null) return null;
        Album album = AlbumMapper.toDomain(entity);
        List<Photo> photos = getPhotosByAlbum(albumId);
        album.getPhotos().addAll(photos.stream().map(Photo::getId).collect(Collectors.toList()));
        return album;
    }

    @Override
    public List<Album> getAllAlbums() {
        List<AlbumEntity> entities = albumDao.getAllAlbums();
        return entities.stream().map(entity -> {
            Album album = AlbumMapper.toDomain(entity);
            List<Photo> photos = getPhotosByAlbum(album.getId());
            album.getPhotos().addAll(photos.stream().map(Photo::getId).collect(Collectors.toList()));
            return album;
        }).collect(Collectors.toList());
    }

    @Override
    public void addPhotoToAlbum(String photoId, String albumId) {
        executorService.execute(() -> {
            PhotoAlbumCrossRef crossRef = new PhotoAlbumCrossRef(photoId, albumId);
            crossRefDao.insertCrossRef(crossRef);

            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_albums")
                        .document(albumId)
                        .update("photos", com.google.firebase.firestore.FieldValue.arrayUnion(photoId));
            }
        });
    }

    @Override
    public void removePhotoFromAlbum(String photoId, String albumId) {
        executorService.execute(() -> {
            crossRefDao.deleteCrossRef(photoId, albumId);

            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_albums")
                        .document(albumId)
                        .update("photos", com.google.firebase.firestore.FieldValue.arrayRemove(photoId));
            }
        });
    }

    @Override
    public void getPrivateAlbumId(OnResultCallback<String> callback) {
        String userId = getCurrentUserId();
        if (userId == null) {
            callback.onResult(null);
            return;
        }

        firestore.collection("photos")
                .document(userId)
                .collection("user_albums")
                .whereEqualTo("name", "Private")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String albumId = task.getResult().getDocuments().get(0).getId();
                        callback.onResult(albumId);
                    } else {
                        Log.e("PhotoRepository", "Error getting Private Album", task.getException());
                        callback.onResult(null);
                    }
                });
    }

    // Interface callback
    public interface OnResultCallback<T> {
        void onResult(T result);
    }


    // --- Sync Methods ---

    public void syncFromFirestore() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        firestore.collection("photos")
                .document(userId)
                .collection("user_photos")
                .orderBy("updatedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("PhotoRepository", "Firestore sync error", error);
                        return;
                    }

                    if (snapshot != null && !snapshot.isEmpty()) {
                        executorService.execute(() -> {
                            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                Photo firestorePhoto = doc.toObject(Photo.class);
                                if (firestorePhoto != null) {
                                    // Kiểm tra xem ảnh đã tồn tại trong Room chưa
                                    PhotoEntity localPhoto = photoDao.getPhotoById(firestorePhoto.getId());

                                    // Chỉ cập nhật nếu ảnh mới hơn hoặc chưa tồn tại
                                    if (localPhoto == null || firestorePhoto.getUpdatedAt() > localPhoto.updatedAt) {
                                        PhotoEntity entity = PhotoMapper.fromDomain(firestorePhoto);
                                        entity.isSynced = true;
                                        photoDao.insertPhoto(entity);
                                    }
                                }
                            }
                        });
                    }
                });
    }

    public void syncFromFirestoreSync() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        try {
            // Lấy dữ liệu từ Firestore một cách đồng bộ
            QuerySnapshot querySnapshot = Tasks.await(
                    firestore.collection("photos")
                            .document(userId)
                            .collection("user_photos")
                            .orderBy("updatedAt", Query.Direction.DESCENDING)
                            .get()
            );

            List<Photo> firestorePhotos = querySnapshot.toObjects(Photo.class);

            executorService.execute(() -> {
                for (Photo firestorePhoto : firestorePhotos) {
                    PhotoEntity localPhoto = photoDao.getPhotoById(firestorePhoto.getId());
                    if (localPhoto == null || firestorePhoto.getUpdatedAt() > localPhoto.updatedAt) {
                        PhotoEntity entity = PhotoMapper.fromDomain(firestorePhoto);
                        entity.isSynced = true;
                        photoDao.insertPhoto(entity);
                    }
                }
                Log.d("PhotoRepository", "Synced " + firestorePhotos.size() + " photos from Firestore");
            });
        } catch (Exception e) {
            Log.e("PhotoRepository", "Sync from Firestore failed", e);
        }
    }
    public void syncLocalPhotosToFirestore() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        executorService.execute(() -> {
            List<PhotoEntity> localPhotos = photoDao.getAllPhotos();
            if (localPhotos.isEmpty()) return;

            CollectionReference photosRef = firestore.collection("photos")
                    .document(userId)
                    .collection("user_photos");

            for (PhotoEntity entity : localPhotos) {
                photosRef.document(entity.id).get()
                        .addOnSuccessListener(doc -> {
                            // Chuyển sang background thread trước khi thao tác với database
                            executorService.execute(() -> {
                                if (!doc.exists()) {
                                    if (entity.isSynced) {
                                        return;
                                    }
                                }
                                Photo firestorePhoto = doc.toObject(Photo.class);
                                boolean shouldUpdate = firestorePhoto == null
                                        || entity.updatedAt > firestorePhoto.getUpdatedAt();

                                if (shouldUpdate) {
                                    Photo photo = PhotoMapper.toDomain(entity);
                                    photosRef.document(photo.getId())
                                            .set(photo)
                                            .addOnSuccessListener(aVoid -> {
                                                // Chạy trên background thread
                                                executorService.execute(() -> {
                                                    entity.isSynced = true;
                                                    photoDao.insertPhoto(entity);
                                                });
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("PhotoRepository", "Sync failed: " + entity.id, e);
                                            });
                                }
                            });
                        })
                        .addOnFailureListener(e -> {
                            Log.e("PhotoRepository", "Firestore fetch error: " + entity.id, e);
                        });
            }
        });
    }
}