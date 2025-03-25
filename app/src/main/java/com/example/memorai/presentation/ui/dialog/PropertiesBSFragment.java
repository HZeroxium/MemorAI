package com.example.memorai.presentation.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memorai.R;
import com.example.memorai.presentation.ui.adapter.ColorPickerAdapter;

import ja.burhanrashid52.photoeditor.PhotoEditor;

public class PropertiesBSFragment extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {

    private Properties mProperties;
    private int currentOpacity = 100; // Mặc định 100%
    private int currentBrushSize = 10; // Mặc định 10px

    public interface Properties {
        void onColorChanged(int colorCode);
        void onOpacityChanged(int opacity);
        void onShapeSizeChanged(int shapeSize);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_properties, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvColor = view.findViewById(R.id.rvColors);
        SeekBar sbOpacity = view.findViewById(R.id.sbOpacity);
        SeekBar sbBrushSize = view.findViewById(R.id.sbSize);

        // Khôi phục trạng thái nếu có
        if (savedInstanceState != null) {
            currentOpacity = savedInstanceState.getInt("BRUSH_OPACITY", currentOpacity);
            currentBrushSize = savedInstanceState.getInt("BRUSH_SIZE", currentBrushSize);
        }

        // Đặt giá trị SeekBar theo trạng thái trước đó
        sbOpacity.setProgress(currentOpacity);
        sbBrushSize.setProgress(currentBrushSize);

        sbOpacity.setOnSeekBarChangeListener(this);
        sbBrushSize.setOnSeekBarChangeListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvColor.setLayoutManager(layoutManager);
        rvColor.setHasFixedSize(true);

        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(getActivity());
        colorPickerAdapter.setOnColorPickerClickListener(colorCode -> {
            if (mProperties != null) {
                dismiss();
                mProperties.onColorChanged(colorCode);
            }
        });
        rvColor.setAdapter(colorPickerAdapter);
    }

    public void setPropertiesChangeListener(Properties properties) {
        this.mProperties = properties;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mProperties != null) {
            if (seekBar.getId() == R.id.sbOpacity) {
                currentOpacity = progress;
                mProperties.onOpacityChanged(progress);
            } else if (seekBar.getId() == R.id.sbSize) {
                currentBrushSize = progress;
                mProperties.onShapeSizeChanged(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("BRUSH_OPACITY", currentOpacity);
        outState.putInt("BRUSH_SIZE", currentBrushSize);
    }
}
