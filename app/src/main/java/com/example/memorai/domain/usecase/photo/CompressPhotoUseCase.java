// domain/usecase/photo/CompressPhotoUseCase.java
package com.example.memorai.domain.usecase.photo;

import com.example.memorai.domain.model.Photo;

import javax.inject.Inject;

public class CompressPhotoUseCase {
    @Inject
    public CompressPhotoUseCase() {
    }

    // Implement image compression logic; dummy implementation below.
    public Photo execute(Photo photo) {
        // Compression logic here...
        return photo;
    }
}
