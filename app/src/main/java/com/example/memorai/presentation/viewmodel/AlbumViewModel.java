// presentation/viewModel/AlbumViewModel.java
package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.usecase.album.CreateAlbumUseCase;
import com.example.memorai.domain.usecase.album.CreateAlbumWithPhotosUseCase;
import com.example.memorai.domain.usecase.album.DeleteAlbumUseCase;
import com.example.memorai.domain.usecase.album.GetAlbumByIdUseCase;
import com.example.memorai.domain.usecase.album.GetAlbumsUseCase;
import com.example.memorai.domain.usecase.album.UpdateAlbumUseCase;
import com.example.memorai.domain.usecase.album.UpdateAlbumWithPhotosUseCase;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AlbumViewModel extends ViewModel {
    private final CreateAlbumUseCase createAlbumUseCase;
    private final GetAlbumsUseCase getAlbumsUseCase;
    private final UpdateAlbumUseCase updateAlbumUseCase;
    private final DeleteAlbumUseCase deleteAlbumUseCase;
    private final GetAlbumByIdUseCase getAlbumByIdUseCase;
    private final CreateAlbumWithPhotosUseCase createAlbumWithPhotosUseCase;
    private final UpdateAlbumWithPhotosUseCase updateAlbumWithPhotosUseCase;

    private final MutableLiveData<List<Album>> allAlbums = new MutableLiveData<>();

    @Inject
    public AlbumViewModel(CreateAlbumUseCase createAlbumUseCase,
                          GetAlbumsUseCase getAlbumsUseCase,
                          UpdateAlbumUseCase updateAlbumUseCase,
                          DeleteAlbumUseCase deleteAlbumUseCase,
                          GetAlbumByIdUseCase getAlbumByIdUseCase,
                          CreateAlbumWithPhotosUseCase createAlbumWithPhotosUseCase,
                          UpdateAlbumWithPhotosUseCase updateAlbumWithPhotosUseCase) {
        this.createAlbumUseCase = createAlbumUseCase;
        this.getAlbumsUseCase = getAlbumsUseCase;
        this.updateAlbumUseCase = updateAlbumUseCase;
        this.deleteAlbumUseCase = deleteAlbumUseCase;
        this.getAlbumByIdUseCase = getAlbumByIdUseCase;
        this.createAlbumWithPhotosUseCase = createAlbumWithPhotosUseCase;
        this.updateAlbumWithPhotosUseCase = updateAlbumWithPhotosUseCase;

    }

    public LiveData<List<Album>> observeAllAlbums() {
        return allAlbums;
    }

    public void loadAllAlbums() {
        new Thread(() -> {
            List<Album> albums = getAlbumsUseCase.execute();
            allAlbums.postValue(albums);
        }).start();
    }

    public void addAlbum(Album album) {
        new Thread(() -> createAlbumUseCase.execute(album)).start();
    }

    public void updateAlbum(Album album) {
        new Thread(() -> updateAlbumUseCase.execute(album)).start();
    }

    public void deleteAlbum(String albumId) {
        new Thread(() -> deleteAlbumUseCase.execute(albumId)).start();
    }

    public void ensureDefaultAlbumExists() {
        new Thread(() -> {
            List<Album> albums = getAlbumsUseCase.execute();
            if (albums == null || albums.isEmpty()) {
                // Default album does not exist, create one
                String defaultCoverPhoto = "android.resource://com.example.memorai/drawable/default_album_cover.jpg";
                Album defaultAlbum = new Album("1",
                        "root", "", defaultCoverPhoto,
                        System.currentTimeMillis(),
                        System.currentTimeMillis());
                createAlbumUseCase.execute(defaultAlbum);
            }
        }).start();
    }

    public LiveData<Album> observeAlbumById(String albumId) {
        MutableLiveData<Album> result = new MutableLiveData<>();
        new Thread(() -> {
            Album album = getAlbumByIdUseCase.execute(albumId);
            result.postValue(album);
        }).start();
        return result;
    }

    public LiveData<Album> getAlbumById(String albumId) {
        return observeAlbumById(albumId);
    }

    public void createAlbumWithPhotos(Album album, List<Photo> photos) {
        new Thread(() -> createAlbumWithPhotosUseCase.execute(album, photos)).start();
    }

    public void updateAlbumWithPhotos(Album album, List<Photo> photos) {
        new Thread(() -> updateAlbumWithPhotosUseCase.execute(album, photos)).start();
    }

}

