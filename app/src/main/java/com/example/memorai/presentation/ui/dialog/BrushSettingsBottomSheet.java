package com.example.memorai.presentation.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.memorai.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ja.burhanrashid52.photoeditor.PhotoEditor;

public class BrushSettingsBottomSheet extends BottomSheetDialogFragment {

    private SeekBar sbSize, sbOpacity;
    private PhotoEditor photoEditor;
    private float currentBrushSize = 10.0f; // 🔹 Giá trị mặc định
    private int currentOpacity = 1;    // 🔹 Giá trị mặc định (1.0 = 100%)

    public BrushSettingsBottomSheet() {
        // Required empty public constructor
    }

    public BrushSettingsBottomSheet(PhotoEditor photoEditor) {
        this.photoEditor = photoEditor;
    }

    public void setPhotoEditor(PhotoEditor photoEditor) {
        this.photoEditor = photoEditor;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_properties, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sbSize = view.findViewById(R.id.sbSize);
        sbOpacity = view.findViewById(R.id.sbOpacity);

        // 🔹 Khôi phục giá trị từ Bundle (nếu có)
        if (savedInstanceState != null) {
            currentBrushSize = savedInstanceState.getFloat("BRUSH_SIZE", currentBrushSize);
            currentOpacity = savedInstanceState.getInt("BRUSH_OPACITY", currentOpacity);
        }

        // 🔹 Set giá trị SeekBar từ biến đã lưu
        sbSize.setProgress((int) currentBrushSize);
        sbOpacity.setProgress((int) (currentOpacity * 100));

        // Xử lý thay đổi kích thước cọ vẽ
        sbSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && photoEditor != null) {
                    photoEditor.setBrushDrawingMode(true); // Đảm bảo chế độ vẽ đang bật
                    photoEditor.setBrushSize((float) progress); // Thiết lập kích thước cọ
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "Brush Size: " + sbSize.getProgress(), Toast.LENGTH_SHORT).show();
            }
        });


        // Xử lý thay đổi độ trong suốt của cọ vẽ
        sbOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    currentOpacity = progress / 100;
                    if (photoEditor != null) {
                        photoEditor.setOpacity(currentOpacity);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(getContext(), "Brush Opacity: " + (int) (currentOpacity * 100), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 🔹 Lưu trạng thái vào Bundle khi Fragment bị destroy
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("BRUSH_SIZE", currentBrushSize);
        outState.putFloat("BRUSH_OPACITY", currentOpacity);
    }
}
