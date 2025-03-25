package com.example.memorai.presentation.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentEditPhotoBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.model.ToolIcon;
import com.example.memorai.presentation.ui.dialog.PropertiesBSFragment;
import com.example.memorai.presentation.ui.dialog.TextEditorDialogFragment;
import com.example.memorai.presentation.viewmodel.EditPhotoViewModel;
import com.example.memorai.presentation.ui.adapter.ToolIconAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.util.ArrayList;
import java.util.List;

import ja.burhanrashid52.photoeditor.PhotoEditor;
import dagger.hilt.android.AndroidEntryPoint;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;

@AndroidEntryPoint
public class EditPhotoFragment extends Fragment {

    private FragmentEditPhotoBinding binding;
    private EditPhotoViewModel editPhotoViewModel;
    private Photo currentPhoto;
    private List<ToolIcon> toolIcons;
    private ToolIconAdapter toolIconAdapter;
    private PhotoViewModel photoViewModel;
    private PhotoEditor photoEditor;
    private Uri photoUri;
    private int undoCount = 0;
    private int redoCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditPhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo ViewModel
        editPhotoViewModel = new ViewModelProvider(this).get(EditPhotoViewModel.class);

        // Khởi tạo PhotoEditor với khả năng phóng to thu nhỏ text
        photoEditor = new PhotoEditor.Builder(getContext(), binding.photoEditorView)
                .setPinchTextScalable(true)
                .build();

        // Khởi tạo danh sách công cụ và RecyclerView
        initToolIcons();
        setupRecyclerView();

        if (getArguments() != null) {
            String photoUrl = getArguments().getString("photo_url", "");
            if (!photoUrl.isEmpty()) {
                currentPhoto = new Photo("id", photoUrl, null, System.currentTimeMillis(), System.currentTimeMillis());
                updatePhotoUI(currentPhoto);
            }
        }

        // Nếu có ảnh được truyền vào Fragment
        if (getArguments() != null) {
            String photoUrl = getArguments().getString("photo_url", "");
            if (!photoUrl.isEmpty()) {
                currentPhoto = new Photo("id", photoUrl, null, System.currentTimeMillis(), System.currentTimeMillis());
                updatePhotoUI(currentPhoto);
            }
        }

        // Lắng nghe thay đổi từ ViewModel khi ảnh được chỉnh sửa
        editPhotoViewModel.getEditedPhoto().observe(getViewLifecycleOwner(), this::updatePhotoUI);

        // Xử lý sự kiện Undo
        binding.imgUndo.setOnClickListener(v -> {
            // Gọi cả undo của PhotoEditor và ViewModel
            if (undoCount > 0) {
                photoEditor.undo();
                undoCount--;
                redoCount++;
                updateUndoRedoButtons();
            }
        });

        // Xử lý sự kiện Redo
        binding.imgRedo.setOnClickListener(v -> {
            if (redoCount > 0) {
                photoEditor.redo();
                redoCount--;
                undoCount++;
                updateUndoRedoButtons();
            }
        });

        // Xử lý sự kiện Lưu ảnh cuối cùng
        // Tích hợp savePhotoToDatabase vào imgSave
        binding.imgSave.setOnClickListener(v -> {
            Photo photo = new Photo(
                    String.valueOf(System.currentTimeMillis()),
                    photoUri.toString()
            );
            if (photo.getFilePath() != null) {
                photoViewModel.addPhoto(photo);
                Toast.makeText(requireContext(), "Photo added successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "Failed to save photo", Toast.LENGTH_SHORT).show();
            }
        });

        updateUndoRedoButtons();
    }

    /**
     * Khởi tạo danh sách các công cụ chỉnh sửa ảnh
     */
    private void initToolIcons() {
        toolIcons = new ArrayList<>();
        toolIcons.add(new ToolIcon(R.drawable.ic_brush, "Brush"));
        toolIcons.add(new ToolIcon(R.drawable.ic_text, "Text"));
        toolIcons.add(new ToolIcon(R.drawable.ic_eraser, "Eraser"));
        toolIcons.add(new ToolIcon(R.drawable.ic_photo_filter, "Filter"));
        toolIcons.add(new ToolIcon(R.drawable.ic_emoji, "Emoji"));
        toolIcons.add(new ToolIcon(R.drawable.ic_sticker, "Sticker"));
    }

    /**
     * Thiết lập RecyclerView để hiển thị danh sách công cụ
     */
    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.rvConstraintTools;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        ToolIconAdapter adapter = new ToolIconAdapter(toolIcons, tool -> {
            switch (tool.getToolName()) {
                case "Brush":
                    photoEditor.setBrushDrawingMode(true);
                    PropertiesBSFragment propertiesBSFragment = new PropertiesBSFragment();
                    propertiesBSFragment.setPropertiesChangeListener(new PropertiesBSFragment.Properties() {
                        @Override
                        public void onColorChanged(int colorCode) {
                            ShapeBuilder shapeBuilder = new ShapeBuilder();
                            shapeBuilder.withShapeColor(colorCode);
                            photoEditor.setShape(shapeBuilder);
                        }

                        @Override
                        public void onOpacityChanged(int opacity) {
                            ShapeBuilder shapeBuilder = new ShapeBuilder();
                            shapeBuilder.withShapeOpacity(opacity);
                            photoEditor.setShape(shapeBuilder);
                        }

                        @Override
                        public void onShapeSizeChanged(int shapeSize) {
                            ShapeBuilder shapeBuilder = new ShapeBuilder();
                            shapeBuilder.withShapeSize((float) shapeSize);
                            photoEditor.setShape(shapeBuilder);
                        }
                    });
                    propertiesBSFragment.show(getParentFragmentManager(), "PropertiesBSFragment");
                    break;
                case "Text":
                    TextEditorDialogFragment textEditorDialog = TextEditorDialogFragment.show();
                    textEditorDialog.setOnTextEditorListener((inputText, colorCode) -> {
                        addTextToPhoto(inputText, colorCode);
                    });
                    textEditorDialog.show(getParentFragmentManager(), "TextEditorDialogFragment");
                    break;
//                case "Eraser":
//                    photoEditor.clearAllText();
//                    undoCount++;
//                    redoCount = 0;
//                    updateUndoRedoButtons();
//                    break;
                default:
                    Toast.makeText(getContext(), "Unknown Tool", Toast.LENGTH_SHORT).show();
                    break;
            }
        });

        recyclerView.setAdapter(adapter);
    }

    /**
     * Thêm text lên ảnh và cập nhật trạng thái ảnh để cho phép Undo/Redo
     */
    private void addTextToPhoto(String text, int color) {
        // Thêm text qua PhotoEditor – đối tượng text sẽ có khả năng di chuyển, thay đổi kích thước
        photoEditor.addText(text, color);

        // Sau khi thêm text, cập nhật state ảnh (ví dụ: cập nhật thời gian) để lưu vào undoStack
        if (currentPhoto != null) {
            currentPhoto = new Photo(
                    currentPhoto.getId(),
                    currentPhoto.getFilePath(),
                    currentPhoto.getTags(),
                    currentPhoto.getCreatedAt(),
                    System.currentTimeMillis()
            );

        }
        undoCount++;
        redoCount = 0; // Reset redo khi có thay đổi mới
        updateUndoRedoButtons();
    }

    /**
     * Cập nhật UI khi ảnh thay đổi
     */
    private void updatePhotoUI(Photo photo) {
        currentPhoto = photo;
        Glide.with(this)
                .load(photo.getFilePath())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.imageViewEditPhoto);

        updateUndoRedoButtons();
    }

    /**
     * Cập nhật trạng thái nút Undo/Redo
     */
    private void updateUndoRedoButtons() {
//        binding.imgUndo.setEnabled(editPhotoViewModel.canUndo());
//        binding.imgRedo.setEnabled(editPhotoViewModel.canRedo());
        binding.imgUndo.setEnabled(undoCount > 0);
        binding.imgRedo.setEnabled(redoCount > 0);

    }
    private void savePhotoToDatabase(Uri uri) {
        Photo photo = new Photo(
                String.valueOf(System.currentTimeMillis()),
                uri.toString()
        );
        if (photo.getFilePath() != null) {
            photoViewModel.addPhoto(photo);
            Toast.makeText(requireContext(), "Photo added successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(requireContext(), "Failed to save photo", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
