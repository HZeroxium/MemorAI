// domain/usecase/photo/SearchPhotosUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

import java.util.List;

import javax.inject.Inject;

public class SearchPhotosUseCase {
    private final PhotoRepository photoRepository;

    @Inject
    public SearchPhotosUseCase(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }

    public List<Photo> execute(String query) {
        return photoRepository.searchPhotos(query);
    }
}
