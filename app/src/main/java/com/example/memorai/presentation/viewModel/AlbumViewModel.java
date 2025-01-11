package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.memorai.domain.model.Album;

import java.util.ArrayList;
import java.util.List;

public class AlbumViewModel extends ViewModel {
    private final MutableLiveData<List<Album>> albumList = new MutableLiveData<>();

    public LiveData<List<Album>> getAlbumList() {
        return albumList;
    }

    public void loadDummyAlbums() {
        List<Album> dummyData = new ArrayList<>();
        dummyData.add(new Album("Album 1", "https://via.placeholder.com/150"));
        dummyData.add(new Album("Album 2", "https://via.placeholder.com/150"));
        albumList.setValue(dummyData);
    }
}
