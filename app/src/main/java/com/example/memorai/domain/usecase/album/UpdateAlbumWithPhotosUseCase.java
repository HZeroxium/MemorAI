// domain/usecase/album/UpdateAlbumWithPhotosUseCase.java
package com.example.memorai.domain.usecase.album;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.AlbumRepository;

import java.util.List;

import javax.inject.Inject;

// domain/usecase/album/CreateAlbumWithPhotosUseCase.java
public class UpdateAlbumWithPhotosUseCase {
    private final AlbumRepository albumRepository;

    @Inject
    public UpdateAlbumWithPhotosUseCase(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public void execute(Album album, List<Photo> photos) {
        // calls the method to do both album insertion + crossRefs
        albumRepository.updateAlbumWithPhotos(album, photos);
    }
}
