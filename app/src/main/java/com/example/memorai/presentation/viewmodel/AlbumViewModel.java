// presentation/viewModel/AlbumViewModel.java
package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.usecase.album.CreateAlbumUseCase;
import com.example.memorai.domain.usecase.album.DeleteAlbumUseCase;
import com.example.memorai.domain.usecase.album.GetAlbumsUseCase;
import com.example.memorai.domain.usecase.album.UpdateAlbumUseCase;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AlbumViewModel extends ViewModel {
    private final CreateAlbumUseCase createAlbumUseCase;
    private final GetAlbumsUseCase getAlbumsUseCase;
    private final UpdateAlbumUseCase updateAlbumUseCase;
    private final DeleteAlbumUseCase deleteAlbumUseCase;

    private final MutableLiveData<List<Album>> allAlbums = new MutableLiveData<>();

    @Inject
    public AlbumViewModel(CreateAlbumUseCase createAlbumUseCase,
                          GetAlbumsUseCase getAlbumsUseCase,
                          UpdateAlbumUseCase updateAlbumUseCase,
                          DeleteAlbumUseCase deleteAlbumUseCase) {
        this.createAlbumUseCase = createAlbumUseCase;
        this.getAlbumsUseCase = getAlbumsUseCase;
        this.updateAlbumUseCase = updateAlbumUseCase;
        this.deleteAlbumUseCase = deleteAlbumUseCase;
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

    public void addAlbum(String albumName) {
        Album album = new Album(UUID.randomUUID().toString(), albumName, "", "", System.currentTimeMillis(), System.currentTimeMillis());
        new Thread(() -> createAlbumUseCase.execute(album)).start();
    }

    public void updateAlbum(Album album) {
        new Thread(() -> updateAlbumUseCase.execute(album)).start();
    }

    public void deleteAlbum(String albumId) {
        new Thread(() -> deleteAlbumUseCase.execute(albumId)).start();
    }
}

