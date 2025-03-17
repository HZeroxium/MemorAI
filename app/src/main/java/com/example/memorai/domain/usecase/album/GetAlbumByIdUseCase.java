// domain/usecase/album/GetaAlbumByIdUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import javax.inject.Inject;

public class GetAlbumByIdUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public GetAlbumByIdUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public Album execute(String albumId) {
        return albumRepository.getAlbumById(albumId);
    }
}
