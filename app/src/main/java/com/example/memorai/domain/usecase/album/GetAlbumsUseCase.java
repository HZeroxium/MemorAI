// domain/usecase/album/GetAllAlbumsUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;

import javax.inject.Inject;

public class GetAlbumsUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public GetAlbumsUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public List<Album> execute() {
        return albumRepository.getAlbums();
    }
}

