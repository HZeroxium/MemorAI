// domain/repository/AlbumRepository.java
package com.example.memorai.domain.repository;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;

import java.util.List;

public interface AlbumRepository {
    List<Album> getAlbums();

    Album getAlbumById(String albumId);

    void addAlbum(Album album);
    void updateAlbum(Album album);

    void deleteAlbum(String albumId);

    // Search albums by keyword
    List<Album> searchAlbums(String query);

    // Get albums sorted by criteria (e.g., "date", "name")
    List<Album> getAlbumsSorted(String sortBy);

    void createAlbumWithPhotos(Album album, List<Photo> photos);

    void updateAlbumWithPhotos(Album album, List<Photo> photos);


}

