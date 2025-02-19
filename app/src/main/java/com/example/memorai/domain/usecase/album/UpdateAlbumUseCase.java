// domain/usecase/album/UpdateAlbumUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import javax.inject.Inject;

public class UpdateAlbumUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public UpdateAlbumUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public void execute(Album album) {
        albumRepository.updateAlbum(album);
    }
}

