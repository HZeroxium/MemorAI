package com.example.memorai.data.repository;

import android.util.Log;

import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.dao.PhotoAlbumCrossRefDao;
import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.data.local.entity.PhotoAlbumCrossRef;
import com.example.memorai.data.mappers.AlbumMapper;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.AlbumRepository;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AlbumRepositoryImpl implements AlbumRepository {

    private final AlbumDao albumDao;
    private final PhotoAlbumCrossRefDao crossRefDao;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Inject
    public AlbumRepositoryImpl(
            AlbumDao albumDao,
            PhotoAlbumCrossRefDao crossRefDao,
            FirebaseFirestore firestore,
            FirebaseAuth firebaseAuth
    ) {
        this.albumDao = albumDao;
        this.crossRefDao = crossRefDao;
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
    }

    private String getUserId() {
        if (firebaseAuth.getCurrentUser() == null) {
            throw new IllegalStateException("User not authenticated");
        }
        return firebaseAuth.getCurrentUser().getUid();
    }

    private CollectionReference getUserAlbumsRef() {
        return firestore.collection("photos").document(getUserId()).collection("user_albums");
    }

    @Override
    public List<Album> getAlbums() {
        return albumDao.getAllAlbums().stream()
                .map(AlbumMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Album getAlbumById(String albumId) {
        AlbumEntity entity = albumDao.getAlbumById(albumId);
        return (entity != null) ? AlbumMapper.toDomain(entity) : null;
    }

    @Override
    public void addAlbum(Album album) {
        AlbumEntity entity = AlbumMapper.fromDomain(album);
        entity.isSynced = false;
        albumDao.insertAlbum(entity);
        syncAlbumToFirebase(entity);
    }

    @Override
    public void updateAlbum(Album album) {
        AlbumEntity entity = AlbumMapper.fromDomain(album);
        entity.isSynced = false;
        albumDao.updateAlbum(entity);
        syncAlbumToFirebase(entity);
    }

    @Override
    public void deleteAlbum(String albumId) {
        albumDao.deleteAlbum(albumId);
        crossRefDao.deleteCrossRefsForAlbum(albumId);
        deleteAlbumFromFirebase(albumId);
    }

    @Override
    public List<Album> searchAlbums(String query) {
        return albumDao.searchAlbums("%" + query + "%").stream()
                .map(AlbumMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Album> getAlbumsSorted(String sortBy) {
        if ("date".equalsIgnoreCase(sortBy)) {
            return albumDao.getAlbumsSortedByDate().stream()
                    .map(AlbumMapper::toDomain)
                    .collect(Collectors.toList());
        } else if ("name".equalsIgnoreCase(sortBy)) {
            return albumDao.getAlbumsSortedByName().stream()
                    .map(AlbumMapper::toDomain)
                    .collect(Collectors.toList());
        } else {
            return getAlbums();
        }
    }

    @Override
    public void createAlbumWithPhotos(Album album, List<Photo> photos) {
        List<String> photoIds = photos.stream().map(Photo::getId).collect(Collectors.toList());
        AlbumEntity entity = new AlbumEntity();
        entity.id = album.getId();
        entity.name = album.getName();
        entity.description = album.getDescription();
        entity.photos = photoIds;
        entity.coverPhotoUrl = album.getCoverPhotoUrl();
        entity.createdAt = album.getCreatedAt();
        entity.updatedAt = System.currentTimeMillis();
        entity.isSynced = false;

        albumDao.insertAlbum(entity);
        for (String photoId : photoIds) {
            PhotoAlbumCrossRef crossRef = new PhotoAlbumCrossRef(photoId, album.getId());
            crossRefDao.insertCrossRef(crossRef);
        }
        syncAlbumToFirebase(entity);
    }

    @Override
    public void updateAlbumWithPhotos(Album album, List<Photo> photos) {
        List<String> photoIds = photos.stream().map(Photo::getId).collect(Collectors.toList());
        AlbumEntity entity = AlbumMapper.fromDomain(album);
        entity.photos = photoIds;
        entity.updatedAt = System.currentTimeMillis();
        entity.isSynced = false;

        albumDao.updateAlbum(entity);
        crossRefDao.deleteCrossRefsForAlbum(album.getId());
        for (String photoId : photoIds) {
            PhotoAlbumCrossRef crossRef = new PhotoAlbumCrossRef(photoId, album.getId());
            crossRefDao.insertCrossRef(crossRef);
        }
        syncAlbumToFirebase(entity);
    }

    private void deleteAlbumFromFirebase(String albumId) {
        if (firebaseAuth.getCurrentUser() == null) return;

        executorService.execute(() -> {
            getUserAlbumsRef().document(albumId).delete()
                    .addOnFailureListener(e -> {
                        Log.e("AlbumRepository", "Failed to delete album from Firebase", e);
                    });
        });
    }


    public void syncFromFirebase() {
        if (firebaseAuth.getCurrentUser() == null) return;

        executorService.execute(() -> {
            try {
                Task<QuerySnapshot> task = getUserAlbumsRef().get();
                task.addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        QuerySnapshot snapshot = task1.getResult();
                        if (snapshot != null) {
                            List<Album> firebaseAlbums = snapshot.toObjects(Album.class);
                            executorService.execute(() -> { // Thêm executorService.execute ở đây
                                for (Album firebaseAlbum : firebaseAlbums) {
                                    AlbumEntity localEntity = albumDao.getAlbumById(firebaseAlbum.getId());
                                    if (localEntity == null || firebaseAlbum.getUpdatedAt() > localEntity.updatedAt) {
                                        AlbumEntity entity = AlbumMapper.fromDomain(firebaseAlbum);
                                        albumDao.insertAlbum(entity);

                                        crossRefDao.deleteCrossRefsForAlbum(firebaseAlbum.getId());
                                        for (String photoId : firebaseAlbum.getPhotos()) {
                                            PhotoAlbumCrossRef crossRef = new PhotoAlbumCrossRef(photoId, firebaseAlbum.getId());
                                            crossRefDao.insertCrossRef(crossRef);
                                        }
                                    }
                                }
                            });
                        }
                    } else {
                        Log.e("AlbumRepository", "Error getting albums", task1.getException());
                    }
                });
            } catch (Exception e) {
                Log.e("AlbumRepository", "Error syncing from Firebase", e);
            }
        });
    }
    public void syncPendingChangesToFirebase() {
        if (firebaseAuth.getCurrentUser() == null) return;

        executorService.execute(() -> {
            // Lấy tất cả album local (hoặc có thể thêm điều kiện nếu cần)
            List<AlbumEntity> localAlbums = albumDao.getAllAlbums();
            for (AlbumEntity entity : localAlbums) {
                syncAlbumToFirebase(entity);
            }
        });
    }

    private void syncAlbumToFirebase(AlbumEntity entity) {
        getUserAlbumsRef().document(entity.id).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot snapshot = task.getResult();
                        // Nếu album chưa tồn tại trên Firebase hoặc bản local mới hơn
                        if (!snapshot.exists() || entity.updatedAt > snapshot.getLong("updatedAt")) {
                            Album album = AlbumMapper.toDomain(entity);
                            getUserAlbumsRef().document(entity.id).set(album)
                                    .addOnSuccessListener(aVoid -> Log.d("AlbumRepository", "Album synced to Firebase"))
                                    .addOnFailureListener(e -> Log.e("AlbumRepository", "Error syncing album", e));
                        }
                    } else {
                        Log.e("AlbumRepository", "Error checking album on Firebase", task.getException());
                    }
                });
    }
}