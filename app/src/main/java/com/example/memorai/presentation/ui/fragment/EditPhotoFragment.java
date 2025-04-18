package com.example.memorai.presentation.ui.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
import androidx.appcompat.app.AlertDialog;
import ja.burhanrashid52.photoeditor.PhotoEditor;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentEditPhotoBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.model.ToolIcon;
import com.example.memorai.presentation.ui.adapter.filters.FilterListener;
import com.example.memorai.presentation.ui.adapter.filters.FilterViewAdapter;
import com.example.memorai.presentation.ui.dialog.StickerBSFragment;
import com.example.memorai.presentation.ui.dialog.TextEditorDialogFragment;
import com.example.memorai.presentation.viewmodel.EditPhotoViewModel;
import com.example.memorai.presentation.ui.adapter.ToolIconAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.example.memorai.presentation.ui.dialog.ShapeBSFragment;
import com.example.memorai.presentation.ui.dialog.EmojiBSFragment;
import com.example.memorai.utils.ShapeTypeWrapper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import ja.burhanrashid52.photoeditor.OnPhotoEditorListener;
import dagger.hilt.android.AndroidEntryPoint;
import ja.burhanrashid52.photoeditor.PhotoFilter;
import ja.burhanrashid52.photoeditor.TextStyleBuilder;
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

    private String photoId;
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
        rootView = binding.getRoot();

        // Initialize constraintSet
        constraintSet = new ConstraintSet();

        // Khởi tạo ViewModel
        editPhotoViewModel = new ViewModelProvider(this).get(EditPhotoViewModel.class);

        // Khởi tạo PhotoEditor với khả năng phóng to thu nhỏ text
        photoEditor = new PhotoEditor.Builder(requireContext(), binding.photoEditorView)
                .setPinchTextScalable(true)
                .build();


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
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        // Thiết lập OnPhotoEditorListener để theo dõi hành động vẽ
        photoEditor.setOnPhotoEditorListener(new OnPhotoEditorListener() {
            @Override
            public void onEditTextChangeListener(View rootView, String text, int colorCode) {
                Log.d("EditPhoto", "Text edited: " + text);
                //photoEditor.editText(rootView, text, colorCode);
                TextEditorDialogFragment textEditorDialogFragment = TextEditorDialogFragment.show(EditPhotoFragment.this, text, colorCode);
                textEditorDialogFragment.setOnTextEditorListener((inputText, color) -> {

                        TextStyleBuilder styleBuilder = new TextStyleBuilder();
                        styleBuilder.withTextColor(color);
                        photoEditor.editText(rootView, inputText, styleBuilder);
                        binding.txtCurrentTool.setText(R.string.label_text);

                });
            }

            @Override
            public void onAddViewListener(ViewType viewType, int numberOfAddedViews) {
                Log.d("EditPhoto", "View added: " + viewType + ", number: " + numberOfAddedViews);
                //if (viewType == ViewType.BRUSH_DRAWING) {
                    undoCount++;
                    //redoCount = 0;
                    updateUndoRedoButtons();
                    //Log.d("EditPhoto", "Brush drawing added. Undo count: " + undoCount);
                //}
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
            photoId = getArguments().getString("photo_id","");
            if (!photoId.isEmpty()) {
                photoViewModel.getPhotoById(photoId).observe(getViewLifecycleOwner(), photo -> {
                    if (photo != null) {
                        currentPhoto = photo;
                        updatePhotoUI(currentPhoto);
                    }
                });
            }
        }
//        SeekBar sizeSlider = binding.sizeSlider;
//        sizeSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                float scale = progress / 100.0f;
//                binding.photoEditorView.getSource().setScaleX(scale);
//                binding.photoEditorView.getSource().setScaleY(scale);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                // No-op
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                // No-op
//            }
//        });

        // Lắng nghe thay đổi từ ViewModel khi ảnh được chỉnh sửa
        editPhotoViewModel.getEditedPhoto().observe(getViewLifecycleOwner(), this::updatePhotoUI);
        // Xử lý sự kiện Undo
        binding.imgUndo.setOnClickListener(v -> {
            // Gọi cả undo của PhotoEditor và ViewModel
            boolean canUndo = editPhotoViewModel.canUndo();;
            if (undoCount > 0 || canUndo) {
                photoEditor.undo();
                undoCount--;
                redoCount++;
                updateUndoRedoButtons();
            }
        });

        // Xử lý sự kiện Redo
        binding.imgRedo.setOnClickListener(v -> {
            boolean canRedo = editPhotoViewModel.canRedo();
            if (redoCount > 0 || canRedo) {
                photoEditor.redo();
                redoCount--;
                undoCount++;
                updateUndoRedoButtons();
            }
        });

        binding.imgSave.setOnClickListener(v -> {
            showSaveDialog();
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

    private void showError(String message) {
        if (isAdded() && !isDetached()) { // Kiểm tra Fragment còn tồn tại
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private File createTempFile() {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "EDIT_" + timeStamp + ".jpg";
            File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            return File.createTempFile(fileName, ".jpg", storageDir);
        } catch (IOException e) {
            Log.e("FileError", "Cannot create temp file", e);
            return null;
        }
    }

    // Xử lý khi lưu thành công
    private void handleSaveSuccess(String imagePath, ProgressDialog progressDialog) {
        requireActivity().runOnUiThread(() -> {
            try {
                progressDialog.dismiss();

                File savedFile = new File(imagePath);
                if (!savedFile.exists()) {
                    throw new IOException("File không tồn tại");
                }

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = calculateInSampleSize(savedFile);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

                if (bitmap != null) {
                    Bitmap safeBitmap = bitmap.copy(bitmap.getConfig(), true);

                    bitmap.recycle();

                    photoViewModel.getPhotoById(photoId).observe(getViewLifecycleOwner(), photo -> {
                        if (photo != null) {
                            photoViewModel.addPhoto(safeBitmap, photo.getTags());
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("SaveError", "Xử lý ảnh lỗi", e);
                showError(R.string.error_processing + e.getMessage());
            }
        });
    }

    // Xử lý khi lưu thất bại
    private void handleSaveFailure(Exception exception, ProgressDialog progressDialog) {
        requireActivity().runOnUiThread(() -> {
            progressDialog.dismiss();
            Log.e("SaveError", "Lưu thất bại", exception);
            showRetryDialog(exception);
        });
    }

    // Tính toán kích thước ảnh phù hợp
    private int calculateInSampleSize(File imageFile) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        // Giảm kích thước ảnh nếu lớn hơn 2048px
        int inSampleSize = 1;
        while (options.outWidth / inSampleSize > 2048 || options.outHeight / inSampleSize > 2048) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }

    // Hiển thị dialog thử lại
    private void showRetryDialog(Exception exception) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.error_saving)
                .setMessage(exception.getMessage())
                .setPositiveButton(R.string.retry_button, (dialog, which) -> {
                    binding.imgSave.performClick();
                })
                .setNegativeButton(R.string.cancel_button, null)
                .show();
    }


    private void showScaleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.scale_dialog_title);

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton(R.string.scale_dialog_ok, (dialog, which) -> {
            int scalePercentage = Integer.parseInt(input.getText().toString());
            float scale = scalePercentage / 100.0f;
            binding.photoEditorView.getSource().setScaleX(scale);
            binding.photoEditorView.getSource().setScaleY(scale);
        });
        builder.setNegativeButton(R.string.scale_dialog_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return rotateBitmapIfNeeded(bitmap, uri);
        } catch (Exception e) {
            Log.e("TakePhotoFragment", "Error loading bitmap", e);
            return null;
        }
    }

    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, Uri uri) {
        try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
            ExifInterface exif = new ExifInterface(inputStream);
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int rotationInDegrees = exifToDegrees(rotation);

            if (rotationInDegrees != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotationInDegrees);
                return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        } catch (Exception e) {
            Log.e("TakePhotoFragment", "Error rotating bitmap", e);
        }
        return bitmap;
    }

    private int exifToDegrees(int exifOrientation) {
        switch (exifOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
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
                    if (brushShapeType instanceof ShapeType.Arrow) {
                        ShapeType.Arrow arrow = (ShapeType.Arrow) brushShapeType;
                        brushBundle.putString("shapeType", "Arrow");
                        brushBundle.putString("arrowPointerLocation", arrow.getPointerLocation().toString());
                    }
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
                    //undoCount++;
                    //updateUndoRedoButtons();
                    break;
                case "Text":
                    TextEditorDialogFragment textEditorDialog = TextEditorDialogFragment.show(this);
//                    textEditorDialog.setOnTextEditorListener((inputText, colorCode) -> {
//                        addTextToPhoto(inputText, colorCode);
//                    });
//                    textEditorDialog.show(getParentFragmentManager(), "TextEditorDialogFragment");
                    textEditorDialog.setOnTextEditorListener(new TextEditorDialogFragment.TextEditorListener() {
                        @Override
                        public void onDone(String inputText, int colorCode) {
                            TextStyleBuilder styleBuilder = new TextStyleBuilder();
                            styleBuilder.withTextColor(colorCode);
                            photoEditor.addText(inputText, styleBuilder);
                            binding.txtCurrentTool.setText(R.string.label_text);
                        }
                    });
                    //undoCount++;
                   // redoCount = 0;
                    //updateUndoRedoButtons();
                    break;
                case "Eraser":
                    photoEditor.brushEraser();
                    //undoCount++;
                   // redoCount = 0;
                    //updateUndoRedoButtons();
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
                                //undoCount++;
                               // redoCount = 0;
                                //updateUndoRedoButtons();
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
                    //undoCount++;
                   // redoCount = 0;
                    //updateUndoRedoButtons();
                    break;
                case "Sticker":
                    showStickerFragment();
                    binding.txtCurrentTool.setText(R.string.label_sticker);
                    //undoCount++;
                   // redoCount = 0;
                    //updateUndoRedoButtons();
                    break;
                default:
                    Toast.makeText(getContext(), R.string.error_unknown_tool, Toast.LENGTH_SHORT).show();
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
                Bitmap resizedBitmap = resizeBitmap(bitmap, 0.2f);
                photoEditor.addImage(resizedBitmap);

                //undoCount++;
                //redoCount = 0;
                //updateUndoRedoButtons();
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
        photoEditor.addText(text, color);

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
      //  redoCount = 0; // Reset redo khi có thay đổi mới
        updateUndoRedoButtons();
    }

    /**
     * Cập nhật UI khi ảnh thay đổi
     */
    private void showSaveDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.save_dialog_title)
                .setMessage(R.string.save_dialog_message)
                .setPositiveButton(R.string.save_button, (dialog, which) -> {
                    ProgressDialog progressDialog = new ProgressDialog(requireContext());
                    progressDialog.setMessage(getString(R.string.saving_progress));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    Executors.newSingleThreadExecutor().execute(() -> {
                        try {
                            File tempFile = createTempFile();
                            if (tempFile == null) {
                                requireActivity().runOnUiThread(() -> {
                                    progressDialog.dismiss();
                                    showError(getString(R.string.error_temp_file));
                                });
                                return;
                            }

                            photoEditor.saveAsFile(tempFile.getAbsolutePath(), new PhotoEditor.OnSaveListener() {
                                @Override
                                public void onSuccess(String imagePath) {
                                    handleSaveSuccess(imagePath, progressDialog);
                                }

                                @Override
                                public void onFailure(Exception exception) {
                                    handleSaveFailure(exception, progressDialog);
                                }
                            });

                        } catch (Exception e) {
                            requireActivity().runOnUiThread(() -> {
                                progressDialog.dismiss();
                                showError(R.string.error_saving + e.getMessage());
                            });
                        }
                    });
                })
                .setNegativeButton(R.string.discard_button, (dialog, which) -> {
                    photoEditor.clearAllViews();
                    requireActivity().getOnBackPressedDispatcher().onBackPressed();
                })
                .setNeutralButton(R.string.exit_button, (dialog, which) -> dialog.dismiss())
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
        Log.d("EditPhoto", "Undo/Redo buttons updated. Undo enabled: " + (undoCount > 0) + ", Redo enabled: " + (redoCount > 0));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
