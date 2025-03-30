// domain/repository/PhotoRepository.java
package com.example.memorai.domain.repository;

import com.example.memorai.domain.model.Photo;

import java.util.List;

public interface PhotoRepository {
    List<Photo> getPhotosByAlbum(String albumId);

    Photo getPhotoById(String photoId);

    void addPhoto(Photo photo);
    void updatePhoto(Photo photo);

    void deletePhoto(String photoId);
    void setPhotoPrivacy(String photoId, boolean isPrivate);

    // Search photos by keyword
    List<Photo> searchPhotos(String query);

    // Get photos sorted by criteria within an album (e.g., "date", "size", "gps")
    List<Photo> getPhotosSorted(String albumId, String sortBy);
}

