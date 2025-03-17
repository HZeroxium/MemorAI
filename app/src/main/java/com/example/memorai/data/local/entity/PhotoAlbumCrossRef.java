// data/local/entity/PhotoAlbumCrossRef.java
package com.example.memorai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(
        tableName = "photo_album_cross_ref",
        primaryKeys = {"photoId", "albumId"}
)
public class PhotoAlbumCrossRef {
    @NonNull
    public String photoId;

    @NonNull
    public String albumId;

    public PhotoAlbumCrossRef(@NonNull String photoId, @NonNull String albumId) {
        this.photoId = photoId;
        this.albumId = albumId;
    }

    // Getters and setters
    @NonNull
    public String getPhotoId() {
        return photoId;
    }

    @NonNull
    public String getAlbumId() {
        return albumId;
    }
}
