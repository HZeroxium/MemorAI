// data/repository/AlbumRepositoryImpl.java
package com.example.memorai.data.repository;

import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.dao.PhotoAlbumCrossRefDao;
import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.data.local.entity.PhotoAlbumCrossRef;
import com.example.memorai.data.mappers.AlbumMapper;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AlbumRepositoryImpl implements AlbumRepository {

    private final AlbumDao albumDao;
    private final PhotoAlbumCrossRefDao crossRefDao;

    @Inject
    public AlbumRepositoryImpl(AlbumDao albumDao, PhotoAlbumCrossRefDao crossRefDao) {
        this.albumDao = albumDao;
        this.crossRefDao = crossRefDao;
    }

    @Override
    public List<Album> getAlbums() {
        return albumDao.getAllAlbums().stream()
                .map(AlbumMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Album getAlbumById(String albumId) {
        AlbumEntity entity = albumDao.getAlbumById(albumId);
        return (entity != null) ? AlbumMapper.toDomain(entity) : null;
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

        // Also delete all cross-references for this album
        crossRefDao.deleteCrossRefsForAlbum(albumId);
    }

    @Override
    public List<Album> searchAlbums(String query) {
        return albumDao.searchAlbums(query).stream()
                .map(AlbumMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Album> getAlbumsSorted(String sortBy) {
        if ("date".equalsIgnoreCase(sortBy)) {
            return albumDao.getAlbumsSortedByDate().stream()
                    .map(AlbumMapper::toDomain)
                    .collect(Collectors.toList());
        } else if ("name".equalsIgnoreCase(sortBy)) {
            return albumDao.getAlbumsSortedByName().stream()
                    .map(AlbumMapper::toDomain)
                    .collect(Collectors.toList());
        } else {
            // default
            return albumDao.getAllAlbums().stream()
                    .map(AlbumMapper::toDomain)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void createAlbumWithPhotos(Album album, List<Photo> photos) {
        // 1) Insert the album
        albumDao.insertAlbum(AlbumMapper.fromDomain(album));

        // 2) Insert cross-ref for each selected photo
        for (Photo p : photos) {
            PhotoAlbumCrossRef crossRef = new PhotoAlbumCrossRef(p.getId(), album.getId());
            crossRefDao.insertCrossRef(crossRef);
        }
    }

    @Override
    public void updateAlbumWithPhotos(Album album, List<Photo> photos) {
        // 1) Update the album
        albumDao.updateAlbum(AlbumMapper.fromDomain(album));

        // 2) Delete all existing cross-refs for this album
        crossRefDao.deleteCrossRefsForAlbum(album.getId());

        // 3) Insert cross-ref for each selected photo
        for (Photo p : photos) {
            PhotoAlbumCrossRef crossRef = new PhotoAlbumCrossRef(p.getId(), album.getId());
            crossRefDao.insertCrossRef(crossRef);
        }
    }
}
