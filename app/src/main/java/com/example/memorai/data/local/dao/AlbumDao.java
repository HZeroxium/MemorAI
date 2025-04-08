// data/local/dao/AlbumDao.java
package com.example.memorai.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memorai.data.local.entity.AlbumEntity;

import java.util.List;

@Dao
public interface AlbumDao {
    @Query("SELECT * FROM album")
    List<AlbumEntity> getAllAlbums();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AlbumEntity> albums);

    @Query("SELECT * FROM album WHERE id = :albumId LIMIT 1")
    AlbumEntity getAlbumById(String albumId);

    @Insert
    void insertAlbum(AlbumEntity album);

    @Update
    void updateAlbum(AlbumEntity album);

    @Query("DELETE FROM album WHERE id = :albumId")
    void deleteAlbum(String albumId);

    // For search and sort features
    @Query("SELECT * FROM album WHERE name LIKE '%' || :query || '%'")
    List<AlbumEntity> searchAlbums(String query);

    @Query("SELECT * FROM album ORDER BY created_at ASC")
    List<AlbumEntity> getAlbumsSortedByDate();

    @Query("SELECT * FROM album ORDER BY name ASC")
    List<AlbumEntity> getAlbumsSortedByName();

    @Query("SELECT * FROM album WHERE isSynced = 0")
    List<AlbumEntity> getUnsyncedAlbums();

}

