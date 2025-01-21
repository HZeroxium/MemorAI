// data/repository/PhotoRepositoryImpl.java
package com.example.memorai.data.repository;

import android.content.Context;

import com.example.memorai.data.local.AppDatabase;
import com.example.memorai.data.local.dao.PhotoDao;
import com.example.memorai.data.local.entity.PhotoEntity;
import com.example.memorai.data.mappers.PhotoMapper;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class PhotoRepositoryImpl implements PhotoRepository {
    private final PhotoDao photoDao;
    private final ExecutorService executor;

    public PhotoRepositoryImpl(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        photoDao = db.photoDao();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public List<Photo> getAllPhotos() {
        List<PhotoEntity> entities = photoDao.getAllPhotosSync();
        return entities.stream().map(PhotoMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Photo> getPhotosByAlbum(int albumId) {
        List<PhotoEntity> entities = photoDao.getPhotosByAlbumSync(albumId);
        return entities.stream().map(PhotoMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void insertPhoto(Photo photo) {
        executor.execute(() -> {
            PhotoEntity entity = PhotoMapper.toEntity(photo);
            photoDao.insertPhoto(entity);
        });
    }

    @Override
    public void updatePhoto(Photo photo) {
        executor.execute(() -> {
            photoDao.updatePhoto(PhotoMapper.toEntity(photo));
        });
    }

    @Override
    public void deletePhoto(Photo photo) {
        executor.execute(() -> {
            photoDao.deletePhoto(PhotoMapper.toEntity(photo));
        });
    }
}
