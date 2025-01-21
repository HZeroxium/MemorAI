// domain/usecase/photo/UpdatePhotoUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

public class UpdatePhotoUseCase {
    private final PhotoRepository repo;

    public UpdatePhotoUseCase(PhotoRepository repo) {
        this.repo = repo;
    }

    public void execute(Photo photo) {
        repo.updatePhoto(photo);
    }
}
