// domain/repository/PhotoRepository.java
package com.example.memorai.domain.repository;

import com.example.memorai.domain.model.Photo;

import java.util.List;

public interface PhotoRepository {
    List<Photo> getAllPhotos();

    List<Photo> getPhotosByAlbum(int albumId);

    void insertPhoto(Photo photo);

    void updatePhoto(Photo photo);

    void deletePhoto(Photo photo);
}
