// domain/usecase/photo/GetPhotosByAlbumUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import java.util.List;

public class GetPhotosByAlbumUseCase {
    private final PhotoRepository repo;

    public GetPhotosByAlbumUseCase(PhotoRepository repo) {
        this.repo = repo;
    }

    public List<Photo> execute(int albumId) {
        return repo.getPhotosByAlbum(albumId);
    }
}
