// presentation/viewmodel/CloudSyncViewModel.java
package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.usecase.cloud.SyncAlbumsUseCase;
import com.example.memorai.domain.usecase.cloud.SyncPhotosUseCase;
import com.example.memorai.domain.usecase.cloud.UploadPhotoToCloudUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class CloudSyncViewModel extends ViewModel {
    private final SyncAlbumsUseCase syncAlbumsUseCase;
    private final SyncPhotosUseCase syncPhotosUseCase;
    private final UploadPhotoToCloudUseCase uploadPhotoToCloudUseCase;

    private final MutableLiveData<Boolean> syncStatus = new MutableLiveData<>();
    private final MutableLiveData<List<Photo>> cloudPhotos = new MutableLiveData<>();

    @Inject
    public CloudSyncViewModel(SyncAlbumsUseCase syncAlbumsUseCase,
                              SyncPhotosUseCase syncPhotosUseCase,
                              UploadPhotoToCloudUseCase uploadPhotoToCloudUseCase) {
        this.syncAlbumsUseCase = syncAlbumsUseCase;
        this.syncPhotosUseCase = syncPhotosUseCase;
        this.uploadPhotoToCloudUseCase = uploadPhotoToCloudUseCase;
    }

    public LiveData<Boolean> getSyncStatus() {
        return syncStatus;
    }

    public LiveData<List<Photo>> getCloudPhotos() {
        return cloudPhotos;
    }

    public void syncPhotos(List<Photo> photos) {
        new Thread(() -> {
            syncPhotosUseCase.execute(photos);
            syncStatus.postValue(true);
        }).start();
    }

    public void uploadPhoto(Photo photo) {
        new Thread(() -> uploadPhotoToCloudUseCase.execute(photo)).start();
    }
}
