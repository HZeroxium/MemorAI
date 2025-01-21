// domain/repository/AlbumRepository.java
package com.example.memorai.domain.repository;

import com.example.memorai.domain.model.Album;

import java.util.List;

public interface AlbumRepository {
    List<Album> getAllAlbums();

    void insertAlbum(Album album);

    void updateAlbum(Album album);

    void deleteAlbum(Album album);
}
