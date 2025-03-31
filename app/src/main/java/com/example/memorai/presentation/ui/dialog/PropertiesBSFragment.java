package com.example.memorai.presentation.ui.dialog;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.memorai.R;
import com.example.memorai.presentation.ui.adapter.ColorPickerAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class PropertiesBSFragment extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {

    // Biến thành viên
    private Properties mProperties;
    private int currentOpacity = 100; // Giá trị mặc định độ trong suốt: 100%
    private int currentBrushSize = 10; // Giá trị mặc định kích thước brush: 10px
    private SharedPreferences prefs;

    // Giao diện callback để thông báo thay đổi thuộc tính
    public interface Properties {
        void onColorChanged(int colorCode);
        void onOpacityChanged(int opacity);
        void onShapeSizeChanged(int shapeSize);
    }

    // Khởi tạo SharedPreferences và đọc giá trị mặc định
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        currentOpacity = prefs.getInt("BRUSH_OPACITY", 100);
        currentBrushSize = prefs.getInt("BRUSH_SIZE", 10);
    }

    // Inflate layout cho BottomSheet
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_properties, container, false);
    }

    // Thiết lập giao diện và sự kiện
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo các view
        RecyclerView rvColor = view.findViewById(R.id.rvColors);
        SeekBar sbOpacity = view.findViewById(R.id.sbOpacity);
        SeekBar sbBrushSize = view.findViewById(R.id.sbSize);

        // Đặt giá trị ban đầu cho SeekBar từ SharedPreferences
        sbOpacity.setProgress(currentOpacity);
        sbBrushSize.setProgress(currentBrushSize);

        // Gán listener cho SeekBar
        sbOpacity.setOnSeekBarChangeListener(this);
        sbBrushSize.setOnSeekBarChangeListener(this);

        // Thiết lập RecyclerView cho color picker
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        rvColor.setLayoutManager(layoutManager);
        rvColor.setHasFixedSize(true);

        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(getActivity());
        colorPickerAdapter.setOnColorPickerClickListener(colorCode -> {
            if (mProperties != null) {
                mProperties.onColorChanged(colorCode);
                dismiss(); // Đóng dialog sau khi chọn màu
            }
        });
        rvColor.setAdapter(colorPickerAdapter);
    }

    // Phương thức để gán listener từ lớp cha
    public void setPropertiesChangeListener(Properties properties) {
        this.mProperties = properties;
    }

    // Xử lý sự kiện thay đổi giá trị SeekBar
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (mProperties != null) {
            SharedPreferences.Editor editor = prefs.edit();
            if (seekBar.getId() == R.id.sbOpacity) {
                currentOpacity = progress;
                mProperties.onOpacityChanged(progress);
                editor.putInt("BRUSH_OPACITY", progress);
            } else if (seekBar.getId() == R.id.sbSize) {
                currentBrushSize = progress;
                mProperties.onShapeSizeChanged(progress);
                editor.putInt("BRUSH_SIZE", progress);
            }
            editor.apply(); // Lưu thay đổi vào SharedPreferences
        }
    }

    // Các phương thức không sử dụng của SeekBar.OnSeekBarChangeListener
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {}

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {}

    // Lưu trạng thái khi fragment bị hủy tạm thời
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("BRUSH_OPACITY", currentOpacity);
        outState.putInt("BRUSH_SIZE", currentBrushSize);
    }
}