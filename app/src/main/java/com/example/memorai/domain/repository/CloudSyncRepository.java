// domain/repository/CloudSyncRepository.java
package com.example.memorai.domain.repository;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;

import java.util.List;

public interface CloudSyncRepository {
    void syncAlbums(List<Album> albums);

    void syncPhotos(List<Photo> photos);

    void uploadPhoto(Photo photo);

    List<Photo> fetchPhotosFromCloud();
}

