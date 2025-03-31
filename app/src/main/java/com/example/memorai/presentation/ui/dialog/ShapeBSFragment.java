//package com.example.memorai.presentation.ui.dialog;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RadioGroup;
//import android.widget.SeekBar;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
//import com.example.memorai.presentation.ui.adapter.ColorPickerAdapter;
//import com.example.memorai.R;
//import com.example.memorai.utils.ShapeTypeWrapper;
//
//
//import ja.burhanrashid52.photoeditor.shape.ShapeType;
//
//public class ShapeBSFragment extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {
//    private Properties mProperties;
//
//    public interface Properties {
//        void onColorChanged(int colorCode);
//        void onOpacityChanged(int opacity);
//        void onShapeSizeChanged(int shapeSize);
//        void onShapePicked(ShapeType shapeType);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_bottom_shapes, container, false);
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        RecyclerView rvColor = view.findViewById(R.id.shapeColors);
//        SeekBar sbOpacity = view.findViewById(R.id.shapeOpacity);
//        SeekBar sbBrushSize = view.findViewById(R.id.shapeSize);
//        RadioGroup shapeGroup = view.findViewById(R.id.shapeRadioGroup);
//
//        // Shape picker
//        shapeGroup.setOnCheckedChangeListener((group, checkedId) -> {
//            if (mProperties != null) {
//                if (checkedId == R.id.lineRadioButton) {
//                    mProperties.onShapePicked(ShapeTypeWrapper.line());
//                } else if (checkedId == R.id.arrowRadioButton) {
//                    mProperties.onShapePicked(ShapeTypeWrapper.arrow());
//                } else if (checkedId == R.id.ovalRadioButton) {
//                    mProperties.onShapePicked(ShapeTypeWrapper.oval());
//                } else if (checkedId == R.id.rectRadioButton) {
//                    mProperties.onShapePicked(ShapeTypeWrapper.rectangle());
//                } else {
//                    mProperties.onShapePicked(ShapeTypeWrapper.brush());
//                }
//            }
//        });
//
//        sbOpacity.setOnSeekBarChangeListener(this);
//        sbBrushSize.setOnSeekBarChangeListener(this);
//
//        // RecyclerView setup
//        rvColor.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
//        rvColor.setHasFixedSize(true);
//
//        ColorPickerAdapter colorPickerAdapter = new ColorPickerAdapter(requireActivity());
//        colorPickerAdapter.setOnColorPickerClickListener(colorCode -> {
//            if (mProperties != null) {
//                dismiss();
//                mProperties.onColorChanged(colorCode);
//            }
//        });
//        rvColor.setAdapter(colorPickerAdapter);
//    }
//
//    public void setPropertiesChangeListener(Properties properties) {
//        this.mProperties = properties;
//    }
//
//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        if (mProperties != null) {
//            if (seekBar.getId() == R.id.shapeOpacity) {
//                mProperties.onOpacityChanged(progress);
//            } else if (seekBar.getId() == R.id.shapeSize) {
//                mProperties.onShapeSizeChanged(progress);
//            }
//        }
//    }
//
//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {}
//
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {}
//}
package com.example.memorai.presentation.ui.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.memorai.R;
import com.example.memorai.presentation.ui.adapter.ColorPickerAdapter;
import com.example.memorai.utils.ShapeTypeWrapper;

import ja.burhanrashid52.photoeditor.shape.ShapeType;

public class ShapeBSFragment extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {

    private Properties mProperties;

    // Sử dụng biến static để lưu trạng thái brush size và opacity giữa các phiên bản của BottomSheet
    private static int savedOpacity = 100; // 100% opacity mặc định
    private static int savedBrushSize = 10; // 10px brush size mặc định

    public interface Properties {
        void onColorChanged(int colorCode);
        void onOpacityChanged(int opacity);
        void onShapeSizeChanged(int shapeSize);
        void onShapePicked(ShapeType shapeType);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_shapes, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvColor = view.findViewById(R.id.shapeColors);
        SeekBar sbOpacity = view.findViewById(R.id.shapeOpacity);
        SeekBar sbBrushSize = view.findViewById(R.id.shapeSize);
        RadioGroup shapeGroup = view.findViewById(R.id.shapeRadioGroup);

        // Xử lý sự kiện chọn shape (sử dụng if-else thay vì switch-case để tránh lỗi constant expression)
        shapeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mProperties != null) {
                if (checkedId == R.id.lineRadioButton) {
                    mProperties.onShapePicked(ShapeTypeWrapper.line());
                } else if (checkedId == R.id.arrowRadioButton) {
                    mProperties.onShapePicked(ShapeTypeWrapper.arrow());
                } else if (checkedId == R.id.ovalRadioButton) {
                    mProperties.onShapePicked(ShapeTypeWrapper.oval());
                } else if (checkedId == R.id.rectRadioButton) {
                    mProperties.onShapePicked(ShapeTypeWrapper.rectangle());
                } else {
                    mProperties.onShapePicked(ShapeTypeWrapper.brush());
                }
            }
        });

        // Đặt giá trị SeekBar từ trạng thái đã lưu
        sbOpacity.setProgress(savedOpacity);
        sbBrushSize.setProgress(savedBrushSize);

        sbOpacity.setOnSeekBarChangeListener(this);
        sbBrushSize.setOnSeekBarChangeListener(this);

        // Cài đặt RecyclerView cho Color Picker
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
            if (seekBar.getId() == R.id.shapeOpacity) {
                savedOpacity = progress; // Cập nhật giá trị opacity đã lưu
                mProperties.onOpacityChanged(progress);
            } else if (seekBar.getId() == R.id.shapeSize) {
                savedBrushSize = progress; // Cập nhật giá trị brush size đã lưu
                mProperties.onShapeSizeChanged(progress);
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Không cần làm gì
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Không cần làm gì
    }
}
