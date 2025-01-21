// data/mappers/PhotoMapper.java
package com.example.memorai.data.mappers;

import com.example.memorai.data.local.entity.PhotoEntity;
import com.example.memorai.domain.model.Photo;

public class PhotoMapper {
    public static Photo toDomain(PhotoEntity entity) {
        Photo photo = new Photo(entity.getPhotoUrl());
        photo.setId(entity.getPhotoId());
        photo.setAlbumId(entity.getAlbumId());
        photo.setCreatedAt(entity.getCreatedAt());
        return photo;
    }

    public static PhotoEntity toEntity(Photo photo) {
        PhotoEntity entity = new PhotoEntity(
                photo.getUrl(),
                photo.getAlbumId(),
                photo.getCreatedAt()
        );
        entity.setPhotoId(photo.getId());
        return entity;
    }
}
