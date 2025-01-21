// domain/usecase/photo/DeletePhotoUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

public class DeletePhotoUseCase {
    private final PhotoRepository repo;

    public DeletePhotoUseCase(PhotoRepository repo) {
        this.repo = repo;
    }

    public void execute(Photo photo) {
        repo.deletePhoto(photo);
    }
}
