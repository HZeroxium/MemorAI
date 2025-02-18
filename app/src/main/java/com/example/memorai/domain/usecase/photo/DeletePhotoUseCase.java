// domain/usecase/photo/DeletePhotoUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.repository.PhotoRepository;

import javax.inject.Inject;

public class DeletePhotoUseCase {
    private final PhotoRepository photoRepository;

    @Inject
    public DeletePhotoUseCase(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public void execute(String photoId) {
        photoRepository.deletePhoto(photoId);
    }
}

