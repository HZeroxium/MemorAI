// data/local/entity/AlbumEntity.java
package com.example.memorai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "album")
public class AlbumEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id = "";

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "cover_photo_url")
    public String coverPhotoUrl;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}


