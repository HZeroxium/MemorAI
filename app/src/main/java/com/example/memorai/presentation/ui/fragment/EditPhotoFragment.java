package com.example.memorai.presentation.ui.fragment;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentEditPhotoBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.model.ToolIcon;
import com.example.memorai.presentation.ui.adapter.filters.FilterListener;
import com.example.memorai.presentation.ui.adapter.filters.FilterViewAdapter;
import com.example.memorai.presentation.ui.dialog.PropertiesBSFragment;
import com.example.memorai.presentation.ui.dialog.StickerBSFragment;
import com.example.memorai.presentation.ui.dialog.TextEditorDialogFragment;
import com.example.memorai.presentation.viewmodel.EditPhotoViewModel;
import com.example.memorai.presentation.ui.adapter.ToolIconAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.example.memorai.presentation.ui.dialog.ShapeBSFragment;
import com.example.memorai.presentation.ui.dialog.EmojiBSFragment;
import com.example.memorai.utils.ShapeTypeWrapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import ja.burhanrashid52.photoeditor.PhotoEditor;
import dagger.hilt.android.AndroidEntryPoint;
import ja.burhanrashid52.photoeditor.PhotoEditorView;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import ja.burhanrashid52.photoeditor.ViewType;
import ja.burhanrashid52.photoeditor.shape.ShapeBuilder;
import ja.burhanrashid52.photoeditor.shape.ShapeType;

@AndroidEntryPoint
public class EditPhotoFragment extends Fragment {

    private FragmentEditPhotoBinding binding;
    private EditPhotoViewModel editPhotoViewModel;
    private Photo currentPhoto;
    private List<ToolIcon> toolIcons;

    private ConstraintLayout rootView;
    private ConstraintSet constraintSet;
    private boolean isFilterVisible = false;
    private ToolIconAdapter toolIconAdapter;
    private PhotoViewModel photoViewModel;
    private PhotoEditor photoEditor;
    private ShapeBuilder shapeBuilder;

    private ShapeType brushShapeType = ShapeTypeWrapper.brush(); // ShapeType mặc định cho Brush
    private int brushColor = -16777216; // Màu mặc định cho Brush (đen)
    private float brushSize = 10f; // Kích thước mặc định cho Brush
    private int brushOpacity = 100; // Độ trong suốt mặc định cho Brush
    private Uri photoUri;
    private int undoCount = 0;
    private int redoCount = 0;
    private String currentTool = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditPhotoBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize rootView using the binding
        rootView = (ConstraintLayout) binding.getRoot();

        // Initialize constraintSet
        constraintSet = new ConstraintSet();

        // Khởi tạo ViewModel
        editPhotoViewModel = new ViewModelProvider(this).get(EditPhotoViewModel.class);

        // Khởi tạo PhotoEditor với khả năng phóng to thu nhỏ text
        photoEditor = new PhotoEditor.Builder(requireContext(), binding.photoEditorView)
                .setPinchTextScalable(true)
                .build();

        // Khởi tạo ShapeBuilder
        shapeBuilder = new ShapeBuilder();
        var photoEditorView = binding.photoEditorView;
        photoEditorView.setOnTouchListener((v, event) -> {
            Log.d("EditPhoto", "Touch event: " + event.getAction());
            return false; // Để PhotoEditor xử lý tiếp
        });

        // Khởi tạo danh sách công cụ và RecyclerView
        initToolIcons();
        setupRecyclerView();

        // Lắng nghe thay đổi từ ViewModel khi ảnh được chỉnh sửa
        editPhotoViewModel.getEditedPhoto().observe(getViewLifecycleOwner(), this::updatePhotoUI);

        // Thiết lập OnPhotoEditorListener để theo dõi hành động vẽ
        photoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
            @Override
            public void onEditTextChangeListener(View rootView, String text, int colorCode) {
                Log.d("EditPhoto", "Text edited: " + text);
                photoEditor.editText(rootView, text, colorCode);
            }

            @Override
            public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
                Log.d("EditPhoto", "View added: " + viewType + ", number: " + numberOfAddedViews);
                if (viewType == ViewType.BRUSH_DRAWING) {
                    undoCount++;
                    redoCount = 0;
                    updateUndoRedoButtons();
                    Log.d("EditPhoto", "Brush drawing added. Undo count: " + undoCount);
                }
            }

            @Override
            public void onRemoveViewListener(ViewType viewType, int numberOfAddedViews) {
                Log.d("EditPhoto", "View removed: " + viewType + ", number: " + numberOfAddedViews);
            }

            @Override
            public void onStartViewChangeListener(ViewType viewType) {
                Log.d("EditPhoto", "Start view change: " + viewType);
            }

            @Override
            public void onStopViewChangeListener(ViewType viewType) {
                Log.d("EditPhoto", "Stop view change: " + viewType);
            }

            @Override
            public void onTouchSourceImage(MotionEvent motionEvent) {
                Log.d("EditPhoto", "Touch source image: " + motionEvent.getAction());
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    undoCount++;
                    redoCount = 0;
                    updateUndoRedoButtons();
                    Log.d("EditPhoto", "Draw action completed. Undo count: " + undoCount);
                }
            }
        });

        // Khởi tạo ShapeBuilder
        shapeBuilder = new ShapeBuilder();

        // Nếu có ảnh được truyền vào Fragment
        if (getArguments() != null) {
            byte[]  photoUrl = getArguments().getByteArray("photo_bitmap");
            if (photoUrl!= null) {
                currentPhoto = new Photo("id", "ok", null, System.currentTimeMillis(), System.currentTimeMillis());
                Bitmap bitmap = BitmapFactory.decodeByteArray(photoUrl, 0, photoUrl.length);
                currentPhoto.setBitmap(bitmap);
                updatePhotoUI(currentPhoto);
            }
        }
        SeekBar sizeSlider = binding.sizeSlider;
        sizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = progress / 100.0f;
                binding.photoEditorView.getSource().setScaleX(scale);
                binding.photoEditorView.getSource().setScaleY(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // No-op
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // No-op
            }
        });

        // Lắng nghe thay đổi từ ViewModel khi ảnh được chỉnh sửa
        editPhotoViewModel.getEditedPhoto().observe(getViewLifecycleOwner(), this::updatePhotoUI);
        // Xử lý sự kiện Undo
        binding.imgUndo.setOnClickListener(v -> {
            // Gọi cả undo của PhotoEditor và ViewModel
            boolean canUndo = photoEditor.undo();
            if (undoCount > 0 || canUndo) {
                photoEditor.undo();
                undoCount--;
                redoCount++;
                updateUndoRedoButtons();
            }
        });

        // Xử lý sự kiện Redo
        binding.imgRedo.setOnClickListener(v -> {
            boolean canRedo = photoEditor.redo();
            if (redoCount > 0 || canRedo) {
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



        // Set up click listener for ic_close
        binding.imgClose.setOnClickListener(v -> {
            if (currentTool != null) {
                switch (currentTool) {
                    case "Brush":
                        photoEditor.setBrushDrawingMode(false); // Disable brush mode
                        break;
                    case "Eraser":
                        photoEditor.setBrushDrawingMode(false); // Disable eraser mode
                        break;
                    case "Filter":
                        showFilter(false); // Hide the filter view
                        break;
                    case "Text":
                        // No specific action needed for Text since the dialog closes itself
                        break;
                    default:
                        // Handle other tools if needed
                        break;
                }
                // Reset the current tool and UI
                currentTool = null;
                binding.txtCurrentTool.setText(R.string.app_name);
            }
            else {
                showSaveDialog();
            }

        });

        updateUndoRedoButtons();
    }

    private void showScaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Scale Image");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            int scalePercentage = Integer.parseInt(input.getText().toString());
            float scale = scalePercentage / 100.0f;
            binding.photoEditorView.getSource().setScaleX(scale);
            binding.photoEditorView.getSource().setScaleY(scale);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
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
            currentTool = tool.getToolName(); // Set the current tool
            switch (tool.getToolName()) {
                case "Brush":
                    photoEditor.setBrushDrawingMode(true);
                    Log.d("EditPhoto", "Brush drawing mode enabled");

                    shapeBuilder = new ShapeBuilder()
                            .withShapeType(brushShapeType)
                            .withShapeColor(brushColor)
                            .withShapeSize(brushSize)
                            .withShapeOpacity(brushOpacity);
                    photoEditor.setShape(shapeBuilder);
                    Log.d("EditPhoto", "ShapeBuilder initialized with saved values for Brush: shapeType=" + brushShapeType + ", color=" + brushColor + ", size=" + brushSize + ", opacity=" + brushOpacity);

                    ShapeBSFragment shapeBSFragment = new ShapeBSFragment();
                    Bundle brushBundle = new Bundle();
                    brushBundle.putInt("color", brushColor);
                    brushBundle.putFloat("size", brushSize);
                    brushBundle.putInt("opacity", brushOpacity);
                    brushBundle.putString("shapeType", brushShapeType.toString());
                    shapeBSFragment.setArguments(brushBundle);

                    shapeBSFragment.setPropertiesChangeListener(new ShapeBSFragment.Properties() {
                        @Override
                        public void onColorChanged(int colorCode) {
                            Log.d("EditPhoto", "Color changed: " + colorCode);
                            brushColor = colorCode;
                            shapeBuilder.withShapeColor(colorCode);
                            photoEditor.setShape(shapeBuilder);
                            binding.txtCurrentTool.setText(R.string.label_brush);
                        }

                        @Override
                        public void onOpacityChanged(int opacity) {
                            Log.d("EditPhoto", "Opacity changed: " + opacity);
                            brushOpacity = opacity;
                            shapeBuilder.withShapeOpacity(opacity);
                            photoEditor.setShape(shapeBuilder);
                        }

                        @Override
                        public void onShapeSizeChanged(int shapeSize) {
                            Log.d("EditPhoto", "Size changed: " + shapeSize);
                            brushSize = (float) shapeSize;
                            shapeBuilder.withShapeSize((float) shapeSize);
                            photoEditor.setShape(shapeBuilder);
                        }

                        @Override
                        public void onShapePicked(ShapeType shapeType) {
                            Log.d("EditPhoto", "Shape picked: " + shapeType);
                            brushShapeType = shapeType;
                            shapeBuilder.withShapeType(shapeType);
                            photoEditor.setShape(shapeBuilder);
                        }
                    });
                    shapeBSFragment.show(getParentFragmentManager(), "ShapeBSFragment");
                    break;
                case "Text":
                    TextEditorDialogFragment textEditorDialog = TextEditorDialogFragment.show();
                    textEditorDialog.setOnTextEditorListener((inputText, colorCode) -> {
                        addTextToPhoto(inputText, colorCode);
                    });
                    textEditorDialog.show(getParentFragmentManager(), "TextEditorDialogFragment");
                    break;
                case "Eraser":
                    photoEditor.brushEraser();
                    undoCount++;
                    redoCount = 0;
                    updateUndoRedoButtons();
                    binding.txtCurrentTool.setText(R.string.label_eraser);                    break;
                case "Filter":
                    // Hiển thị rvFilterView với hiệu ứng
                    showFilter(true);
                    binding.txtCurrentTool.setText(R.string.label_filter);

                    // Thiết lập RecyclerView cho bộ lọc
                    RecyclerView rvFilterView = binding.rvFilterView;
                    rvFilterView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                    FilterViewAdapter filterAdapter = new FilterViewAdapter(new FilterListener() {
                        @Override
                        public void onFilterSelected(@Nullable PhotoFilter photoFilter) {
                            if (photoFilter != null) {
                                photoEditor.setFilterEffect(photoFilter);
                                Log.d("EditPhoto", "Applied filter: " + photoFilter.name());
                                // Update undo/redo counts
                                undoCount++;
                                redoCount = 0;
                                updateUndoRedoButtons();
                            }
                        }
                    });
                    rvFilterView.setAdapter(filterAdapter);
                    break;
                case "Emoji":
                    EmojiBSFragment emojiBSFragment = new EmojiBSFragment();
                    emojiBSFragment.setEmojiListener(emojiUnicode -> {
                        photoEditor.addText(emojiUnicode, Color.BLACK);
                    });
                    emojiBSFragment.show(getParentFragmentManager(), "EmojiBSFragment");
                    break;
                case "Sticker":
                    showStickerFragment();
                    binding.txtCurrentTool.setText(R.string.label_sticker);
                    break;
                default:
                    Toast.makeText(getContext(), "Unknown Tool", Toast.LENGTH_SHORT).show();
                    break;
            }



        });

        recyclerView.setAdapter(adapter);
    }

    private void showStickerFragment() {
        StickerBSFragment stickerFragment = new StickerBSFragment();
        stickerFragment.setStickerListener(new StickerBSFragment.StickerListener() {
            @Override
            public void onStickerClick(Bitmap bitmap) {
                // Thêm sticker vào ảnh bằng PhotoEditor
                Bitmap resizedBitmap = resizeBitmap(bitmap, 0.2f);
                photoEditor.addImage(resizedBitmap);
                Log.d("EditPhotoFragment","Sticker upload image successfully");
                // Cập nhật số lượng undo/redo (nếu bạn đang quản lý thủ công)

                undoCount++;
                redoCount = 0;
                updateUndoRedoButtons();
            }
        });
        stickerFragment.show(getParentFragmentManager(), "StickerBSFragment");
    }

    private Bitmap resizeBitmap(Bitmap bitmap, float scaleFactor) {
        int width = Math.round(bitmap.getWidth() * scaleFactor);
        int height = Math.round(bitmap.getHeight() * scaleFactor);
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    /**
     * Thêm text lên ảnh và cập nhật trạng thái ảnh để cho phép Undo/Redo
     */
    private void showFilter(boolean isVisible) {
        isFilterVisible = isVisible;
        binding.rvFilterView.setVisibility(isVisible ? View.VISIBLE : View.GONE);

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.setDuration(350);
        changeBounds.setInterpolator(new android.view.animation.AnticipateOvershootInterpolator(1.0f));
        TransitionManager.beginDelayedTransition(rootView, changeBounds);

        constraintSet.clone(rootView);
        int rvFilterId = binding.rvFilterView.getId();

        if (isVisible) {
            constraintSet.clear(rvFilterId, ConstraintSet.START);
            constraintSet.connect(rvFilterId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            constraintSet.connect(rvFilterId, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        } else {
            constraintSet.connect(rvFilterId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.END);
            constraintSet.clear(rvFilterId, ConstraintSet.END);
        }

        constraintSet.applyTo(rootView);
    }
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
    private void showSaveDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Save Changes")
                .setMessage("Do you want to save your changes before exiting?")
                .setPositiveButton("Save", (dialog, which) -> {
                    // Save the photo (you can call your existing save logic here)
                    binding.imgSave.performClick(); // Simulate clicking the save button
                    requireActivity().getOnBackPressedDispatcher().onBackPressed(); // Proceed with back press
                })
                .setNegativeButton("Discard", (dialog, which) -> {
                    // Discard changes and proceed with back press
                    photoEditor.clearAllViews(); // Clear any unsaved changes
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                })
                .setNeutralButton("Cancel", (dialog, which) -> {
                    // Do nothing, stay in the fragment
                    dialog.dismiss();
                })
                .show();
    }
    private void updatePhotoUI(Photo photo) {
        if (photo != null && photo.getFilePath() != null) {
            // Dùng Glide để tải ảnh vào PhotoEditorView
            Glide.with(this)
                    .load(photo.getBitmap())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(binding.photoEditorView.getSource());
            binding.photoEditorView.getSource().setScaleType(ImageView.ScaleType.FIT_CENTER);
            this.photoUri = Uri.parse(photo.getFilePath()); // Lưu photoUri để dùng khi lưu ảnh
            Log.d("EditPhoto", "Image set in PhotoEditorView with Glide: " + photo.getFilePath());
        } else {
            Log.d("EditPhoto", "Photo or file path is null");
            binding.photoEditorView.getSource().setImageResource(R.drawable.placeholder_image);
        }

        updateUndoRedoButtons();
    }
    public static Bitmap decodeBase64ToImage(String base64) {
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e("Photo", "Error decoding Base64 to image", e);
            return null;
        }
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
