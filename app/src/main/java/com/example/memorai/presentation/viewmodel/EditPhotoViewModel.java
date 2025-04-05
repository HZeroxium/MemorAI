package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.usecase.photo.EditPhotoUseCase;

import java.util.Stack;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EditPhotoViewModel extends ViewModel {
    private final EditPhotoUseCase editPhotoUseCase;
    private final MutableLiveData<Photo> editedPhoto = new MutableLiveData<>();

    // Sử dụng Stack để lưu lại trạng thái ảnh cho Undo/Redo
    private final Stack<Photo> undoStack = new Stack<>();
    private final Stack<Photo> redoStack = new Stack<>();

    @Inject
    public EditPhotoViewModel(EditPhotoUseCase editPhotoUseCase) {
        this.editPhotoUseCase = editPhotoUseCase;
    }

    public LiveData<Photo> getEditedPhoto() {
        return editedPhoto;
    }

    /**
     * Cập nhật ảnh mới và lưu trạng thái cũ vào undoStack.
     */
    public void updatePhoto(Photo newPhoto) {
        // Chạy trên background thread nếu cần
        new Thread(() -> {
            if (editedPhoto.getValue() != null) {
                undoStack.push(editedPhoto.getValue());
            }
            redoStack.clear();
            editPhotoUseCase.execute(newPhoto);
            editedPhoto.postValue(newPhoto);
        }).start();
    }

    /**
     * Undo: Lấy trạng thái ảnh trước đó từ undoStack và cập nhật redoStack.
     */
    public void undo() {
        if (!undoStack.isEmpty()) {
            new Thread(() -> {
                Photo previousPhoto = undoStack.pop();
                // Lưu trạng thái hiện tại vào redoStack
                if (editedPhoto.getValue() != null) {
                    redoStack.push(editedPhoto.getValue());
                }
                editedPhoto.postValue(previousPhoto);
            }).start();
        }
    }

    /**
     * Redo: Lấy trạng thái ảnh từ redoStack và cập nhật undoStack.
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            new Thread(() -> {
                Photo nextPhoto = redoStack.pop();
                if (editedPhoto.getValue() != null) {
                    undoStack.push(editedPhoto.getValue());
                }
                editedPhoto.postValue(nextPhoto);
            }).start();
        }
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
