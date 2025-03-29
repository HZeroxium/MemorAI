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

    @Query("SELECT * FROM photo WHERE id = :photoId LIMIT 1")
    PhotoEntity getPhotoById(String photoId);

    @Insert
    void insertPhoto(PhotoEntity photo);

    @Update
    void updatePhoto(PhotoEntity photo);

    @Delete
    void deletePhoto(PhotoEntity photo);

    // Search logic: can be file_path or tags
    @Query("SELECT * FROM photo WHERE file_path LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    List<PhotoEntity> searchPhotos(String query);

    // If you need a "global" sort or something else, you can do it here
    @Query("SELECT * FROM photo ORDER BY created_at ASC")
    List<PhotoEntity> getAllPhotosSortedByDate();

    @Query("SELECT * FROM photo ORDER BY file_path ASC")
    List<PhotoEntity> getAllPhotosSortedByName();

    @Query("UPDATE photo SET is_private = :isPrivate WHERE id = :photoId")
    void updatePhotoPrivacy(String photoId, boolean isPrivate);
}
