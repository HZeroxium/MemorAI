// domain/usecase/photo/AddPhotoUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;

public class AddPhotoUseCase {
    private final PhotoRepository repo;

    public AddPhotoUseCase(PhotoRepository repo) {
        this.repo = repo;
    }

    public void execute(Photo photo) {
        repo.insertPhoto(photo);
    }
}
