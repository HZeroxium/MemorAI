// data/repository/AlbumRepositoryImpl.java
package com.example.memorai.data.repository;

import android.content.Context;

import com.example.memorai.data.local.AppDatabase;
import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.mappers.AlbumMapper;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AlbumRepositoryImpl implements AlbumRepository {

    private final AlbumDao albumDao;
    private final ExecutorService executor;

    public AlbumRepositoryImpl(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        albumDao = db.albumDao();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public List<Album> getAllAlbums() {
        return albumDao.getAllAlbums()
                .stream()
                .map(AlbumMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void insertAlbum(Album album) {
        executor.execute(() -> albumDao.insertAlbum(AlbumMapper.toEntity(album)));
    }

    @Override
    public void updateAlbum(Album album) {
        executor.execute(() -> albumDao.updateAlbum(AlbumMapper.toEntity(album)));
    }

    @Override
    public void deleteAlbum(Album album) {
        executor.execute(() -> albumDao.deleteAlbum(AlbumMapper.toEntity(album)));
    }
}
