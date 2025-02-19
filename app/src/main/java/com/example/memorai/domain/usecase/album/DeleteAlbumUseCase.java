// domain/usecase/album/DeleteAlbumUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.repository.AlbumRepository;

import javax.inject.Inject;

public class DeleteAlbumUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public DeleteAlbumUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public void execute(String albumId) {
        albumRepository.deleteAlbum(albumId);
    }
}

