// domain/usecase/cloud/SyncPhotosUseCase.java
package com.example.memorai.domain.usecase.cloud;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.CloudSyncRepository;

import java.util.List;

import javax.inject.Inject;

public class SyncPhotosUseCase {
    private final CloudSyncRepository cloudSyncRepository;

    @Inject
    public SyncPhotosUseCase(CloudSyncRepository cloudSyncRepository) {
        this.cloudSyncRepository = cloudSyncRepository;
    }

    public void execute(List<Photo> photos) {
        cloudSyncRepository.syncPhotos(photos);
    }
}
