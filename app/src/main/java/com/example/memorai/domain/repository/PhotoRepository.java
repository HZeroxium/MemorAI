// domain/repository/PhotoRepository.java
package com.example.memorai.domain.repository;

import android.content.Context;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;

import java.util.List;

public interface PhotoRepository {
    List<Photo> getPhotosByAlbum(String albumId);

    Photo getPhotoById(String photoId);

    void addPhoto(Photo photo);
    void updatePhoto(Photo photo);

    void deletePhoto(String photoId);
    void deleteAllPhotos();

    void setPhotoPrivacy(String photoId, boolean isPrivate);

    // Search photos by keyword
    List<Photo> searchPhotos(String query);

    // Get photos sorted by criteria within an album (e.g., "date", "size", "gps")
    List<Photo> getPhotosSorted(String albumId, String sortBy);

    void addAlbum(Album album);
    void updateAlbum(Album album);
    void deleteAlbum(String albumId);
    Album getAlbumById(String albumId);
    List<Album> getAllAlbums();
    void addPhotoToAlbum(String photoId, String albumId);
    void removePhotoFromAlbum(String photoId, String albumId);
    List<Photo> getAllPhotos();

    void syncFromFirestore();

    public void syncFromFirestoreSync();

    void syncLocalPhotosToFirestore();
}

