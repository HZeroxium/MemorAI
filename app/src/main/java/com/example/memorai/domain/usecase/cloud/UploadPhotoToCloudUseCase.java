// domain/usecase/cloud/UploadPhotoToCloudUseCase.java
package com.example.memorai.domain.usecase.cloud;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.CloudSyncRepository;

import javax.inject.Inject;

public class UploadPhotoToCloudUseCase {
    private final CloudSyncRepository cloudSyncRepository;

    @Inject
    public UploadPhotoToCloudUseCase(CloudSyncRepository cloudSyncRepository) {
        this.cloudSyncRepository = cloudSyncRepository;
    }

    public void execute(Photo photo) {
        cloudSyncRepository.uploadPhoto(photo);
    }
}
