// data/local/dao/PhotoAlbumCrossRefDao.java
package com.example.memorai.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.memorai.data.local.entity.PhotoAlbumCrossRef;

import java.util.List;

@Dao
public interface PhotoAlbumCrossRefDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCrossRef(PhotoAlbumCrossRef crossRef);

    @Delete
    void deleteCrossRef(PhotoAlbumCrossRef crossRef);

    @Query("SELECT * FROM photo_album_cross_ref WHERE albumId = :albumId")
    List<PhotoAlbumCrossRef> getCrossRefsForAlbum(String albumId);

    @Query("SELECT * FROM photo_album_cross_ref WHERE photoId = :photoId")
    List<PhotoAlbumCrossRef> getCrossRefsForPhoto(String photoId);
}
