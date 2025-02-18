// presentation/viewModel/AlbumViewModel.java
package com.example.memorai.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.memorai.data.repository.AlbumRepositoryImpl;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.repository.AlbumRepository;
import com.example.memorai.domain.usecase.album.CreateAlbumUseCase;
import com.example.memorai.domain.usecase.album.DeleteAlbumUseCase;
import com.example.memorai.domain.usecase.album.GetAlbumsUseCase;
import com.example.memorai.domain.usecase.album.UpdateAlbumUseCase;

import java.util.List;

public class AlbumViewModel extends AndroidViewModel {
    private final CreateAlbumUseCase addAlbumUC;
    private final GetAlbumsUseCase getAllAlbumsUC;
    private final UpdateAlbumUseCase updateAlbumUC;
    private final DeleteAlbumUseCase deleteAlbumUC;

    private final MutableLiveData<List<Album>> allAlbums = new MutableLiveData<>();

    public AlbumViewModel(@NonNull Application app) {
        super(app);
        AlbumRepository repo = new AlbumRepositoryImpl(app);
        addAlbumUC = new CreateAlbumUseCase(repo);
        getAllAlbumsUC = new GetAlbumsUseCase(repo);
        updateAlbumUC = new UpdateAlbumUseCase(repo);
        deleteAlbumUC = new DeleteAlbumUseCase(repo);
    }

    public LiveData<List<Album>> observeAllAlbums() {
        return allAlbums;
    }

    public void loadAllAlbums() {
        new Thread(() -> {
            List<Album> albums = getAllAlbumsUC.execute();
            allAlbums.postValue(albums);
        }).start();
    }

    public void addAlbum(String name) {
        Album album = new Album(name, System.currentTimeMillis());
        addAlbumUC.execute(album);
    }

    public void updateAlbum(Album album) {
        updateAlbumUC.execute(album);
    }

    public void deleteAlbum(Album album) {
        deleteAlbumUC.execute(album);
    }
}
