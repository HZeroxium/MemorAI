// domain/usecase/photo/GetPhotoDetailsUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import javax.inject.Inject;

public class GetPhotoDetailsUseCase {
    private final PhotoRepository photoRepository;

    @Inject
    public GetPhotoDetailsUseCase(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public Photo execute(String photoId) {
        return photoRepository.getPhotoById(photoId);
    }
}

