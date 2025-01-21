// data/mappers/AlbumMapper.java
package com.example.memorai.data.mappers;

import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.domain.model.Album;

public class AlbumMapper {
    public static Album toDomain(AlbumEntity entity) {
        Album album = new Album(entity.getName());
        album.setId(entity.getId());
        album.setCreatedAt(entity.getCreatedAt());
        return album;
    }

    public static AlbumEntity toEntity(Album album) {
        AlbumEntity entity = new AlbumEntity(
                album.getName(),
                album.getCreatedAt()
        );
        entity.setId(album.getId());
        return entity;
    }
}
