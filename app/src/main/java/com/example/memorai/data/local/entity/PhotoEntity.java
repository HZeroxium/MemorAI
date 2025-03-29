// data/local/entity/PhotoEntity.java
package com.example.memorai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "photo")
public class PhotoEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    public String id = "";

    @ColumnInfo(name = "file_path")
    public String filePath;

    @ColumnInfo(name = "is_private", defaultValue = "0")
    public boolean isPrivate;

    @ColumnInfo(name = "tags")
    public String tags; // comma-separated

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;


}