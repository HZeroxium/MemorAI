// data/repository/PhotoRepositoryImpl.java
package com.example.memorai.data.repository;

import com.example.memorai.data.local.dao.PhotoAlbumCrossRefDao;
import com.example.memorai.data.local.dao.PhotoDao;
import com.example.memorai.data.local.entity.PhotoAlbumCrossRef;
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
    private final PhotoAlbumCrossRefDao crossRefDao;

    @Inject
    public PhotoRepositoryImpl(PhotoDao photoDao, PhotoAlbumCrossRefDao crossRefDao) {
        this.photoDao = photoDao;
        this.crossRefDao = crossRefDao;
    }

    @Override
    public Photo getPhotoById(String photoId) {
        PhotoEntity entity = photoDao.getPhotoById(photoId);
        return (entity != null) ? PhotoMapper.toDomain(entity) : null;
    }

    @Override
    public void addPhoto(Photo photo) {
        photoDao.insertPhoto(PhotoMapper.fromDomain(photo));
        PhotoAlbumCrossRef crossRef = new PhotoAlbumCrossRef(photo.getId(), "1");
        crossRefDao.insertCrossRef(crossRef);
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

        // Also delete cross-ref
        crossRefDao.deleteCrossRefsForPhoto(photoId);
    }

    @Override
    public List<Photo> searchPhotos(String query) {
        return photoDao.searchPhotos(query).stream()
                .map(PhotoMapper::toDomain)
                .collect(Collectors.toList());
    }

    // If we want photos by an album, we do it via cross-ref or a specialized method
    @Override
    public List<Photo> getPhotosByAlbum(String albumId) {
        // Not possible directly from PhotoDao, so we do a manual approach or a transaction query
        List<PhotoAlbumCrossRef> crossRefs = crossRefDao.getCrossRefsForAlbum(albumId);
        return crossRefs.stream()
                .map(crossRef -> getPhotoById(crossRef.getPhotoId()))
                .collect(Collectors.toList());

    }

    @Override
    public List<Photo> getPhotosSorted(String albumId, String sortBy) {
        // Also not directly possible, because the data is no longer stored in PhotoEntity
        // We can do something like "throw new UnsupportedOperationException(...)" or handle a custom query
        throw new UnsupportedOperationException(
                "Use cross-ref or a specialized method to get sorted photos by album."
        );
    }
}
