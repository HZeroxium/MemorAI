// domain/usecase/album/GetAllAlbumsUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;

public class GetAllAlbumsUseCase {
    private final AlbumRepository repo;

    public GetAllAlbumsUseCase(AlbumRepository repo) {
        this.repo = repo;
    }

    public List<Album> execute() {
        return repo.getAllAlbums();
    }
}
