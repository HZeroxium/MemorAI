// domain/usecase/album/AddAlbumUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

public class AddAlbumUseCase {
    private final AlbumRepository repo;

    public AddAlbumUseCase(AlbumRepository repo) {
        this.repo = repo;
    }

    public void execute(Album album) {
        repo.insertAlbum(album);
    }
}
