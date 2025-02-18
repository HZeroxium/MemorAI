// data/repository/AlbumRepositoryImpl.java
package com.example.memorai.data.repository;

import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.data.mappers.AlbumMapper;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AlbumRepositoryImpl implements AlbumRepository {

    private final AlbumDao albumDao;

    @Inject
    public AlbumRepositoryImpl(AlbumDao albumDao) {
        this.albumDao = albumDao;
    }

    @Override
    public List<Album> getAlbums() {
        List<AlbumEntity> entities = albumDao.getAllAlbums();
        return entities.stream().map(AlbumMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public Album getAlbumById(String albumId) {
        AlbumEntity entity = albumDao.getAlbumById(albumId);
        return entity != null ? AlbumMapper.toDomain(entity) : null;
    }

    @Override
    public void addAlbum(Album album) {
        albumDao.insertAlbum(AlbumMapper.fromDomain(album));
    }

    @Override
    public void updateAlbum(Album album) {
        albumDao.updateAlbum(AlbumMapper.fromDomain(album));
    }

    @Override
    public void deleteAlbum(String albumId) {
        AlbumEntity entity = albumDao.getAlbumById(albumId);
        if (entity != null) {
            albumDao.deleteAlbum(entity);
        }
    }

    @Override
    public List<Album> searchAlbums(String query) {
        List<AlbumEntity> entities = albumDao.searchAlbums(query);
        return entities.stream().map(AlbumMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Album> getAlbumsSorted(String sortBy) {
        List<AlbumEntity> entities;
        if ("date".equalsIgnoreCase(sortBy)) {
            entities = albumDao.getAlbumsSortedByDate();
        } else if ("name".equalsIgnoreCase(sortBy)) {
            entities = albumDao.getAlbumsSortedByName();
        } else {
            entities = albumDao.getAllAlbums();
        }
        return entities.stream().map(AlbumMapper::toDomain).collect(Collectors.toList());
    }
}
