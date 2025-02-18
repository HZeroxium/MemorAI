// domain/usecase/cloud/SyncAlbumsUseCase.java
package com.example.memorai.domain.usecase.cloud;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.CloudSyncRepository;

import java.util.List;

import javax.inject.Inject;

public class SyncAlbumsUseCase {
    private final CloudSyncRepository cloudSyncRepository;

    @Inject
    public SyncAlbumsUseCase(CloudSyncRepository cloudSyncRepository) {
        this.cloudSyncRepository = cloudSyncRepository;
    }

    public void execute(List<Album> albums) {
        cloudSyncRepository.syncAlbums(albums);
    }
}
