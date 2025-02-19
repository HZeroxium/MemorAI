// domain/usecase/photo/GetPhotosByAlbumUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import java.util.List;

import javax.inject.Inject;

public class GetPhotosByAlbumUseCase {
    private final PhotoRepository photoRepository;

    @Inject
    public GetPhotosByAlbumUseCase(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public List<Photo> execute(String albumId) {
        return photoRepository.getPhotosByAlbum(albumId);
    }
}

