// data/local/dao/PhotoDao.java
package com.example.memorai.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memorai.data.local.entity.PhotoEntity;

import java.util.List;

@Dao
public interface PhotoDao {
    @Insert
    long insertPhoto(PhotoEntity photo);

    @Update
    void updatePhoto(PhotoEntity photo);

    @Delete
    void deletePhoto(PhotoEntity photo);

    @Query("SELECT * FROM photos ORDER BY created_at DESC")
    List<PhotoEntity> getAllPhotosSync(); // new sync method

    @Query("SELECT * FROM photos WHERE album_id = :albumId ORDER BY created_at DESC")
    List<PhotoEntity> getPhotosByAlbumSync(int albumId); // new sync method
}
