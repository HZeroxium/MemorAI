// presentation/ui/dialog/ModalBottomSheetAddMenu.java
package com.example.memorai.presentation.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.memorai.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ModalBottomSheetAddMenu extends BottomSheetDialogFragment {

    private BottomSheetListener listener;

    public ModalBottomSheetAddMenu(BottomSheetListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_add_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button buttonAddAlbum = view.findViewById(R.id.buttonAddAlbum);
        Button buttonImportPhoto = view.findViewById(R.id.buttonImportPhoto);
        Button buttonTakePhoto = view.findViewById(R.id.buttonTakePhoto);

        buttonAddAlbum.setOnClickListener(v -> {
            listener.onAddAlbum();
            dismiss();
        });

        buttonImportPhoto.setOnClickListener(v -> {
            listener.onImportPhoto();
            dismiss();
        });

        buttonTakePhoto.setOnClickListener(v -> {
            listener.onTakePhoto();
            dismiss();
        });
    }

    public interface BottomSheetListener {
        void onAddAlbum();

        void onImportPhoto();

        void onTakePhoto();
    }
}
