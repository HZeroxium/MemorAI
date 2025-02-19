// data/repository/PhotoRepositoryImpl.java
package com.example.memorai.data.repository;

import com.example.memorai.data.local.dao.PhotoDao;
import com.example.memorai.data.local.entity.PhotoEntity;
import com.example.memorai.data.mappers.PhotoMapper;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PhotoRepositoryImpl implements PhotoRepository {

    private final PhotoDao photoDao;

    @Inject
    public PhotoRepositoryImpl(PhotoDao photoDao) {
        this.photoDao = photoDao;
    }

    @Override
    public List<Photo> getPhotosByAlbum(String albumId) {
        List<PhotoEntity> entities = photoDao.getPhotosByAlbum(albumId);
        return entities.stream().map(PhotoMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Photo getPhotoById(String photoId) {
        PhotoEntity entity = photoDao.getPhotoById(photoId);
        return entity != null ? PhotoMapper.toDomain(entity) : null;
    }

    @Override
    public void addPhoto(Photo photo) {
        photoDao.insertPhoto(PhotoMapper.fromDomain(photo));
    }

    @Override
    public void updatePhoto(Photo photo) {
        photoDao.updatePhoto(PhotoMapper.fromDomain(photo));
    }

    @Override
    public void deletePhoto(String photoId) {
        PhotoEntity entity = photoDao.getPhotoById(photoId);
        if (entity != null) {
            photoDao.deletePhoto(entity);
        }
    }

    @Override
    public List<Photo> searchPhotos(String query) {
        List<PhotoEntity> entities = photoDao.searchPhotos(query);
        return entities.stream().map(PhotoMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Photo> getPhotosSorted(String albumId, String sortBy) {
        List<PhotoEntity> entities;
        if ("date".equalsIgnoreCase(sortBy)) {
            entities = photoDao.getPhotosSortedByDate(albumId);
        } else if ("name".equalsIgnoreCase(sortBy)) {
            entities = photoDao.getPhotosSortedByName(albumId);
        } else {
            entities = photoDao.getPhotosByAlbum(albumId);
        }
        return entities.stream().map(PhotoMapper::toDomain).collect(Collectors.toList());
    }
}
