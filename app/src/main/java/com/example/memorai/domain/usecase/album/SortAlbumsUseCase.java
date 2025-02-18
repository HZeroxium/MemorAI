// domain/usecase/album/SortAlbumsUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;

import javax.inject.Inject;

public class SortAlbumsUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public SortAlbumsUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public List<Album> execute(String sortBy) {
        return albumRepository.getAlbumsSorted(sortBy);
    }
}
