// presentation/viewModel/PhotoViewModel.java
package com.example.memorai.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorai.data.repository.PhotoRepositoryImpl;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.repository.PhotoRepository;
import com.example.memorai.domain.usecase.photo.AddPhotoUseCase;
import com.example.memorai.domain.usecase.photo.DeletePhotoUseCase;
import com.example.memorai.domain.usecase.photo.GetAllPhotosUseCase;
import com.example.memorai.domain.usecase.photo.GetPhotosByAlbumUseCase;
import com.example.memorai.domain.usecase.photo.UpdatePhotoUseCase;

import java.util.List;

public class PhotoViewModel extends AndroidViewModel {
    private final AddPhotoUseCase addPhotoUC;
    private final GetAllPhotosUseCase getAllPhotosUC;
    private final GetPhotosByAlbumUseCase getPhotosByAlbumUC;
    private final UpdatePhotoUseCase updatePhotoUC;
    private final DeletePhotoUseCase deletePhotoUC;

    private final MutableLiveData<List<Photo>> allPhotos = new MutableLiveData<>();
    private final MutableLiveData<List<Photo>> albumPhotos = new MutableLiveData<>();

    public PhotoViewModel(@NonNull Application app) {
        super(app);
        PhotoRepository repo = new PhotoRepositoryImpl(app);
        addPhotoUC = new AddPhotoUseCase(repo);
        getAllPhotosUC = new GetAllPhotosUseCase(repo);
        getPhotosByAlbumUC = new GetPhotosByAlbumUseCase(repo);
        updatePhotoUC = new UpdatePhotoUseCase(repo);
        deletePhotoUC = new DeletePhotoUseCase(repo);
    }

    public LiveData<List<Photo>> observeAllPhotos() {
        return allPhotos;
    }

    public LiveData<List<Photo>> observeAlbumPhotos() {
        return albumPhotos;
    }

    public void loadAllPhotos() {
        List<Photo> result = getAllPhotosUC.execute();
        allPhotos.setValue(result);
    }

    public void loadPhotosByAlbum(int albumId) {
        List<Photo> result = getPhotosByAlbumUC.execute(albumId);
        albumPhotos.setValue(result);
    }

    public void addPhoto(String url, int albumId) {
        Photo p = new Photo(url);
        p.setAlbumId(albumId);
        p.setCreatedAt(System.currentTimeMillis());
        addPhotoUC.execute(p);
    }

    public void updatePhoto(Photo p) {
        updatePhotoUC.execute(p);
    }

    public void deletePhoto(Photo p) {
        deletePhotoUC.execute(p);
    }
}
