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
    @Insert
    long insertAlbum(AlbumEntity album);

    @Update
    void updateAlbum(AlbumEntity album);

    @Delete
    void deleteAlbum(AlbumEntity album);

    @Query("SELECT * FROM albums ORDER BY created_at DESC")
    List<AlbumEntity> getAllAlbums(); // new sync method
}
