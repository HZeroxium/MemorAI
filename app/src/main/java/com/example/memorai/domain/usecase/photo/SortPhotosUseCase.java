// domain/usecase/photo/SortPhotosUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import java.util.List;

import javax.inject.Inject;

public class SortPhotosUseCase {
    private final PhotoRepository photoRepository;

    @Inject
    public SortPhotosUseCase(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public List<Photo> execute(String albumId, String sortBy) {
        return photoRepository.getPhotosSorted(albumId, sortBy);
    }
}

