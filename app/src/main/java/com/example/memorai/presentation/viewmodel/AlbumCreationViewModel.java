// presentation/viewmodel/AlbumCreationViewModel.java
package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Photo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/**
 * Stores user’s photo selections while creating a new album.
 */
@HiltViewModel
public class AlbumCreationViewModel extends ViewModel {

    private final MutableLiveData<List<Photo>> selectedPhotos = new MutableLiveData<>(new ArrayList<>());

    @Inject
    public AlbumCreationViewModel() {
        // Default constructor for Hilt
    }

    public LiveData<List<Photo>> getSelectedPhotos() {
        return selectedPhotos;
    }

    public void addPhoto(Photo photo) {
        List<Photo> current = new ArrayList<>(selectedPhotos.getValue());
        if (!current.contains(photo)) {
            current.add(photo);
            selectedPhotos.setValue(current);
        }
    }

    public void removePhoto(Photo photo) {
        List<Photo> current = new ArrayList<>(selectedPhotos.getValue());
        if (current.contains(photo)) {
            current.remove(photo);
            selectedPhotos.setValue(current);
        }
    }

    public void setPhotos(List<Photo> photos) {
        selectedPhotos.setValue(new ArrayList<>(photos));
    }

    public void clear() {
        selectedPhotos.setValue(new ArrayList<>());
    }
}
