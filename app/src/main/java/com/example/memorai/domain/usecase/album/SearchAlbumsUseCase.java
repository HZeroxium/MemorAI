// domain/usecase/album/SearchAlbumsUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;

import javax.inject.Inject;

public class SearchAlbumsUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public SearchAlbumsUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public List<Album> execute(String query) {
        return albumRepository.searchAlbums(query);
    }
}
