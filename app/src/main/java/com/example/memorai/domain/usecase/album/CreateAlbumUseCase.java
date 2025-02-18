// domain/usecase/album/AddAlbumUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import javax.inject.Inject;

public class CreateAlbumUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public CreateAlbumUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public void execute(Album album) {
        albumRepository.addAlbum(album);
    }
}
