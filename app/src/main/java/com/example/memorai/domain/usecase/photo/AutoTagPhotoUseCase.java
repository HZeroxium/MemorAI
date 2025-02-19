// domain/usecase/photo/AutoTagPhotoUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import javax.inject.Inject;

public class AutoTagPhotoUseCase {
    private final PhotoRepository photoRepository;

    @Inject
    public AutoTagPhotoUseCase(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    // This method triggers auto-tagging logic (to be implemented in repository/service)
    public void execute(Photo photo) {
        photoRepository.updatePhoto(photo);
    }
}
