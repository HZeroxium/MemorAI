// data/local/entity/PhotoEntity.java
package com.example.memorai.data.local.entity;

import static androidx.room.ForeignKey.CASCADE;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "photo",
        foreignKeys = @ForeignKey(entity = AlbumEntity.class,
                parentColumns = "id",
                childColumns = "album_id",
                onDelete = CASCADE),
        indices = {@Index(value = "album_id")})
public class PhotoEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id = "";

    @ColumnInfo(name = "album_id")
    public String albumId;

    @ColumnInfo(name = "file_path")
    public String filePath;

    @ColumnInfo(name = "tags")
    public String tags; // Comma-separated tags

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}


