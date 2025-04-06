package com.example.memorai.data.mappers;

import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.domain.model.Album;

import java.util.ArrayList;

public class AlbumMapper {
    public static Album toDomain(AlbumEntity entity) {
        return new Album(
                entity.id,
                entity.name,
                entity.description,
                entity.photos != null ? entity.photos : new ArrayList<>(),  // Ensure non-null list
                entity.coverPhotoUrl,
                entity.createdAt,
                entity.updatedAt
        );
    }

    public static AlbumEntity fromDomain(Album album) {
        AlbumEntity entity = new AlbumEntity();
        entity.id = album.getId();
        entity.name = album.getName();
        entity.description = album.getDescription();
        entity.photos = album.getPhotos(); // Map photos
        entity.coverPhotoUrl = album.getCoverPhotoUrl();
        entity.createdAt = album.getCreatedAt();
        entity.updatedAt = album.getUpdatedAt();
        entity.isSynced = true;
        return entity;
    }
}
