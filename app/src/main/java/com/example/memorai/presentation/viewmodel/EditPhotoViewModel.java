// presentation/viewmodel/EditPhotoViewModel.java
package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.usecase.photo.EditPhotoUseCase;


import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditPhotoViewModel extends ViewModel {
    private final EditPhotoUseCase editPhotoUseCase;
    private final MutableLiveData<Photo> editedPhoto = new MutableLiveData<>();

    @Inject
    public EditPhotoViewModel(EditPhotoUseCase editPhotoUseCase) {
        this.editPhotoUseCase = editPhotoUseCase;
    }

    public LiveData<Photo> getEditedPhoto() {
        return editedPhoto;
    }

    public void updatePhoto(Photo photo) {
        new Thread(() -> {
            editPhotoUseCase.execute(photo);
            editedPhoto.postValue(photo);
        }).start();
    }

}
