// data/mappers/PhotoMapper.java
package com.example.memorai.data.mappers;

import com.example.memorai.data.local.entity.PhotoEntity;
import com.example.memorai.domain.model.Photo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PhotoMapper {
    public static Photo toDomain(PhotoEntity entity) {
        List<String> tags = (entity.tags != null && !entity.tags.isEmpty())
                ? Arrays.asList(entity.tags.split(","))
                : Collections.emptyList();

        return new Photo(
                entity.id,
                entity.filePath,
                tags,
                entity.createdAt,
                entity.updatedAt
        );
    }

    public static PhotoEntity fromDomain(Photo photo) {
        PhotoEntity entity = new PhotoEntity();
        entity.id = photo.getId();
        entity.filePath = photo.getFilePath();
        entity.tags = String.join(",", photo.getTags());
        entity.createdAt = photo.getCreatedAt();
        entity.updatedAt = photo.getUpdatedAt();
        entity.isPrivate = photo.getIsPrivate();
        return entity;
    }
}