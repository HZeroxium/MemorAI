// presentation/viewModel/PhotoViewModel.java
package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.usecase.photo.AddPhotoUseCase;
import com.example.memorai.domain.usecase.photo.DeletePhotoUseCase;
import com.example.memorai.domain.usecase.photo.EditPhotoUseCase;
import com.example.memorai.domain.usecase.photo.GetPhotosByAlbumUseCase;
import com.example.memorai.domain.usecase.photo.SearchPhotosUseCase;
import com.example.memorai.domain.usecase.photo.SortPhotosUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class PhotoViewModel extends ViewModel {
    private final AddPhotoUseCase addPhotoUseCase;
    private final GetPhotosByAlbumUseCase getPhotosByAlbumUseCase;
    private final EditPhotoUseCase editPhotoUseCase;
    private final DeletePhotoUseCase deletePhotoUseCase;
    private final SearchPhotosUseCase searchPhotosUseCase;
    private final SortPhotosUseCase sortPhotosUseCase;

    private final MutableLiveData<List<Photo>> albumPhotos = new MutableLiveData<>();

    @Inject
    public PhotoViewModel(AddPhotoUseCase addPhotoUseCase,
                          GetPhotosByAlbumUseCase getPhotosByAlbumUseCase,
                          EditPhotoUseCase editPhotoUseCase,
                          DeletePhotoUseCase deletePhotoUseCase,
                          SearchPhotosUseCase searchPhotosUseCase,
                          SortPhotosUseCase sortPhotosUseCase) {
        this.addPhotoUseCase = addPhotoUseCase;
        this.getPhotosByAlbumUseCase = getPhotosByAlbumUseCase;
        this.editPhotoUseCase = editPhotoUseCase;
        this.deletePhotoUseCase = deletePhotoUseCase;
        this.searchPhotosUseCase = searchPhotosUseCase;
        this.sortPhotosUseCase = sortPhotosUseCase;
    }

    public LiveData<List<Photo>> observeAllPhotos() {
        return albumPhotos;
    }

    public void loadAllPhotos() {
        new Thread(() -> {
            List<Photo> photos = getPhotosByAlbumUseCase.execute(null);
            albumPhotos.postValue(photos);
        }).start();
    }

    public void loadPhotosByAlbum(String albumId) {
        new Thread(() -> {
            List<Photo> photos = getPhotosByAlbumUseCase.execute(albumId);
            albumPhotos.postValue(photos);
        }).start();
    }

    public void addPhoto(Photo photo) {
        new Thread(() -> addPhotoUseCase.execute(photo)).start();
    }

    public void updatePhoto(Photo photo) {
        new Thread(() -> editPhotoUseCase.execute(photo)).start();
    }

    public void deletePhoto(String photoId) {
        new Thread(() -> deletePhotoUseCase.execute(photoId)).start();
    }

    public void searchPhotos(String query) {
        new Thread(() -> {
            List<Photo> photos = searchPhotosUseCase.execute(query);
            albumPhotos.postValue(photos);
        }).start();
    }

    public void sortPhotos(String albumId, String sortBy) {
        new Thread(() -> {
            List<Photo> photos = sortPhotosUseCase.execute(albumId, sortBy);
            albumPhotos.postValue(photos);
        }).start();
    }
}
