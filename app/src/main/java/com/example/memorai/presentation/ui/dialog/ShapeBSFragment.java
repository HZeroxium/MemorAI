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

import ja.burhanrashid52.photoeditor.shape.ArrowPointerLocation;
import ja.burhanrashid52.photoeditor.shape.ShapeType;

public class ShapeBSFragment extends BottomSheetDialogFragment implements SeekBar.OnSeekBarChangeListener {

    private Properties mProperties;

    // Sử dụng biến static để lưu trạng thái brush size và opacity giữa các phiên bản của BottomSheet
    private static int savedOpacity = 100; // 100% opacity mặc định
    private static int savedBrushSize = 10; // 10px brush size mặc định

    private static ShapeType savedShapeType = ShapeTypeWrapper.brush(); // Mặc định là Brush

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

        // Đọc giá trị từ Bundle và cập nhật các trạng thái đã lưu
        Bundle arguments = getArguments();
        if (arguments != null) {
            String shapeTypeString = arguments.getString("shapeType");
            if (shapeTypeString != null) {
                switch (shapeTypeString) {
                    case "Brush":
                        savedShapeType = ShapeTypeWrapper.brush();
                        break;
                    case "Line":
                        savedShapeType = ShapeTypeWrapper.line();
                        break;
                    case "Oval":
                        savedShapeType = ShapeTypeWrapper.oval();
                        break;
                    case "Rectangle":
                        savedShapeType = ShapeTypeWrapper.rectangle();
                        break;
                    case "Arrow":
                        String pointerLocationString = arguments.getString("arrowPointerLocation", ArrowPointerLocation.START.toString());
                        ArrowPointerLocation pointerLocation = ArrowPointerLocation.valueOf(pointerLocationString);
                        savedShapeType = ShapeTypeWrapper.arrow(pointerLocation);
                        break;
                    default:
                        savedShapeType = ShapeTypeWrapper.brush();
                        break;
                }
            }

            // Cập nhật savedOpacity và savedBrushSize từ Bundle
            savedOpacity = arguments.getInt("opacity", savedOpacity);
            savedBrushSize = (int) arguments.getFloat("size", savedBrushSize);
        }

        // Thiết lập trạng thái của RadioGroup dựa trên savedShapeType
        if (savedShapeType == ShapeTypeWrapper.line()) {
            shapeGroup.check(R.id.lineRadioButton);
        } else if (savedShapeType instanceof ShapeType.Arrow) {
            shapeGroup.check(R.id.arrowRadioButton);
        } else if (savedShapeType == ShapeTypeWrapper.oval()) {
            shapeGroup.check(R.id.ovalRadioButton);
        } else if (savedShapeType == ShapeTypeWrapper.rectangle()) {
            shapeGroup.check(R.id.rectRadioButton);
        } else {
            shapeGroup.check(R.id.brushRadioButton); // Mặc định là Brush
        }

        // Xử lý sự kiện chọn shape
        shapeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mProperties != null) {
                if (checkedId == R.id.lineRadioButton) {
                    savedShapeType = ShapeTypeWrapper.line();
                    mProperties.onShapePicked(savedShapeType);
                } else if (checkedId == R.id.arrowRadioButton) {
                    // Mặc định sử dụng Arrow với pointerLocation là START
                    // Nếu bạn có UI để chọn pointerLocation, hãy cập nhật giá trị này
                    savedShapeType = ShapeTypeWrapper.arrow(ArrowPointerLocation.START);
                    mProperties.onShapePicked(savedShapeType);
                } else if (checkedId == R.id.ovalRadioButton) {
                    savedShapeType = ShapeTypeWrapper.oval();
                    mProperties.onShapePicked(savedShapeType);
                } else if (checkedId == R.id.rectRadioButton) {
                    savedShapeType = ShapeTypeWrapper.rectangle();
                    mProperties.onShapePicked(savedShapeType);
                } else {
                    savedShapeType = ShapeTypeWrapper.brush();
                    mProperties.onShapePicked(savedShapeType);
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
                //dismiss();
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
