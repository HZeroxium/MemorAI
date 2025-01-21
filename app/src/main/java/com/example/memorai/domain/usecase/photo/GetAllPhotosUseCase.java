// domain/usecase/photo/GetAllPhotosUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import java.util.List;

public class GetAllPhotosUseCase {
    private final PhotoRepository repo;

    public GetAllPhotosUseCase(PhotoRepository repo) {
        this.repo = repo;
    }

    public List<Photo> execute() {
        return repo.getAllPhotos();
    }
}
