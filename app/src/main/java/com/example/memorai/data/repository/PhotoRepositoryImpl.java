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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
            photoDao.updatePhotoPrivacy(photoId, isPrivate);
            String userId = getCurrentUserId();
            if (userId != null) {
                firestore.collection("photos")
                        .document(userId)
                        .collection("user_photos")
                        .document(photoId)
                        .update("private", isPrivate);
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

                                    albumsRef.document(document.getId())
                                            .update("photos", photoIds, "updatedAt", System.currentTimeMillis())
                                            .addOnSuccessListener(aVoid -> Log.d("AlbumRepository", "Removed photoId from album: " + document.getId()))
                                            .addOnFailureListener(e -> Log.e("AlbumRepository", "Failed to update album: " + document.getId(), e));
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
                                        photoDao.insertPhoto(entity);
                                    }
                                }
                            }
                        });
                    }
                });
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
                                                    photoDao.updatePhoto(entity);
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