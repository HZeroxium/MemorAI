// domain/usecase/album/UpdateAlbumUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

public class UpdateAlbumUseCase {
    private final AlbumRepository repo;

    public UpdateAlbumUseCase(AlbumRepository repo) {
        this.repo = repo;
    }

    public void execute(Album album) {
        repo.updateAlbum(album);
    }
}
