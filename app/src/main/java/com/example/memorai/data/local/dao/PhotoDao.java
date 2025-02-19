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
    @Query("SELECT * FROM photo WHERE album_id = :albumId")
    List<PhotoEntity> getPhotosByAlbum(String albumId);

    @Query("SELECT * FROM photo WHERE id = :photoId LIMIT 1")
    PhotoEntity getPhotoById(String photoId);

    @Insert
    void insertPhoto(PhotoEntity photo);

    @Update
    void updatePhoto(PhotoEntity photo);

    @Delete
    void deletePhoto(PhotoEntity photo);

    // For search and sort features
    @Query("SELECT * FROM photo WHERE file_path LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    List<PhotoEntity> searchPhotos(String query);

    @Query("SELECT * FROM photo WHERE album_id = :albumId ORDER BY created_at ASC")
    List<PhotoEntity> getPhotosSortedByDate(String albumId);

    @Query("SELECT * FROM photo WHERE album_id = :albumId ORDER BY file_path ASC")
    List<PhotoEntity> getPhotosSortedByName(String albumId);
}
