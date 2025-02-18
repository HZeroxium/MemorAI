// domain/usecase/photo/EditPhotoUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import javax.inject.Inject;

public class EditPhotoUseCase {
    private final PhotoRepository photoRepository;

    @Inject
    public EditPhotoUseCase(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public void execute(Photo photo) {
        photoRepository.updatePhoto(photo);
    }
}

