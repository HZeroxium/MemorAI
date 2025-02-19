// data/local/dao/AlbumDao.java
package com.example.memorai.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memorai.data.local.entity.AlbumEntity;

import java.util.List;

@Dao
public interface AlbumDao {
    @Query("SELECT * FROM album")
    List<AlbumEntity> getAllAlbums();

    @Query("SELECT * FROM album WHERE id = :albumId LIMIT 1")
    AlbumEntity getAlbumById(String albumId);

    @Insert
    void insertAlbum(AlbumEntity album);

    @Update
    void updateAlbum(AlbumEntity album);

    @Delete
    void deleteAlbum(AlbumEntity album);

    // For search and sort features
    @Query("SELECT * FROM album WHERE name LIKE '%' || :query || '%'")
    List<AlbumEntity> searchAlbums(String query);

    @Query("SELECT * FROM album ORDER BY created_at ASC")
    List<AlbumEntity> getAlbumsSortedByDate();

    @Query("SELECT * FROM album ORDER BY name ASC")
    List<AlbumEntity> getAlbumsSortedByName();
}

