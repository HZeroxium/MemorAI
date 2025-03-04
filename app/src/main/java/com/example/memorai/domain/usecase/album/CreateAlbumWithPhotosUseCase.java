// domain/usecase/album/CreateAlbumWithPhotosUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;

import javax.inject.Inject;

// domain/usecase/album/CreateAlbumWithPhotosUseCase.java
public class CreateAlbumWithPhotosUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public CreateAlbumWithPhotosUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public void execute(Album album, List<Photo> photos) {
        // calls the method to do both album insertion + crossRefs
        albumRepository.createAlbumWithPhotos(album, photos);
    }
}
