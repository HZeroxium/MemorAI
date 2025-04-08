// domain/usecase/photo/AddPhotoUseCase.java
package com.example.memorai.domain.usecase.photo;

import android.content.Context;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import javax.inject.Inject;

public class AddPhotoUseCase {
    private final PhotoRepository photoRepository;

    @Inject
    public AddPhotoUseCase(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public void execute(Photo photo) {
        photoRepository.addPhoto(photo);
    }
}

