// data/local/entity/PhotoEntity.java
package com.example.memorai.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "photos",
        foreignKeys = @ForeignKey(
                entity = AlbumEntity.class,
                parentColumns = "id",
                childColumns = "album_id",
                onDelete = ForeignKey.CASCADE
        )
)
public class PhotoEntity {

    @PrimaryKey(autoGenerate = true)
    private int photoId;

    @ColumnInfo(name = "photo_url")
    private String photoUrl;

    @ColumnInfo(name = "album_id", index = true)
    private int albumId;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    // Ví dụ: có thể thêm trường "tags", "isFavorite", "description"...
//     @ColumnInfo(name = "tags")
//     private String tags;

    public PhotoEntity(String photoUrl, int albumId, long createdAt) {
        this.photoUrl = photoUrl;
        this.albumId = albumId;
        this.createdAt = createdAt;
    }

    // Getters/Setters
    public int getPhotoId() {
        return photoId;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
