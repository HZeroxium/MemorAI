// data/repository/AlbumRepositoryImpl.java
package com.example.memorai.data.repository;

import android.content.Context;

import com.example.memorai.data.local.AppDatabase;
import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.data.mappers.AlbumMapper;
import com.example.memorai.domain.model.Album;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AlbumRepositoryImpl {

    private final AlbumDao albumDao;
    private final ExecutorService executorService;

    public AlbumRepositoryImpl(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        albumDao = db.albumDao();
        executorService = Executors.newSingleThreadExecutor();
    }

    public List<Album> getAllAlbums() {
        List<AlbumEntity> entities = albumDao.getAllAlbums();
        return entities.stream().map(AlbumMapper::toDomain).collect(Collectors.toList());
    }

    public void insertAlbum(final AlbumEntity album) {
        executorService.execute(() -> albumDao.insertAlbum(album));
    }

    public void updateAlbum(final AlbumEntity album) {
        executorService.execute(() -> albumDao.updateAlbum(album));
    }

    public void deleteAlbum(final AlbumEntity album) {
        executorService.execute(() -> albumDao.deleteAlbum(album));
    }
}
