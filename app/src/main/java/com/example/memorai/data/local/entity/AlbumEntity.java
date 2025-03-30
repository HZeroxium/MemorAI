package com.example.memorai.data.local.entity;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;


import java.util.List;

@Entity(tableName = "album")
@TypeConverters(BitmapConverter.class) // Apply the converter
public class AlbumEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id = "";

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "photos")
    @TypeConverters(ListConverter.class)  // Convert List<String> to JSON
    public List<String> photos;

    @ColumnInfo(name = "cover_photo_url")
    public String coverPhotoUrl;

    @ColumnInfo(name = "created_at")
    public long createdAt;

    @ColumnInfo(name = "updated_at")
    public long updatedAt;
}
