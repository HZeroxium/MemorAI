// domain/usecase/album/DeleteAlbumUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

public class DeleteAlbumUseCase {
    private final AlbumRepository repo;

    public DeleteAlbumUseCase(AlbumRepository repo) {
        this.repo = repo;
    }

    public void execute(Album album) {
        repo.deleteAlbum(album);
    }
}
