package com.example.memorai.presentation.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentPhotoListBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoSection;
import com.example.memorai.presentation.ui.adapter.PhotoSectionAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoListFragment extends Fragment {

    private FragmentPhotoListBinding binding;
    private PhotoViewModel photoViewModel;
    private PhotoSectionAdapter adapter;
    private boolean isSelectionMode = false;

    private static final String VIEW_MODE_COMFORTABLE = "COMFORTABLE";
    private static final String VIEW_MODE_DAY = "DAY";
    private static final String VIEW_MODE_MONTH = "MONTH";
    private String currentViewMode = VIEW_MODE_COMFORTABLE;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo toolbar
        binding.toolbarPhotoList.setTitle(getString(R.string.photos));
        binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_more_vert);
        binding.toolbarPhotoList.setNavigationOnClickListener(this::showViewModePopup);

        // Ẩn "Select All" mặc định
        binding.checkBoxSelectAll.setVisibility(View.GONE);
        binding.checkBoxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isSelectionMode)
                return;
            if (isChecked) {
                adapter.selectAllInSection(0);
            } else {
                adapter.clearSectionSelection(0);
            }
        });

        // Thiết lập RecyclerView
        setupRecyclerView();

        // Khởi tạo PhotoViewModel
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), this::handlePhotoList);

        // Quản lý menu toolbar bằng MenuHost
        setupMenu();

        // Xử lý nút xóa khi chọn
        binding.buttonDeleteSelected.setOnClickListener(v -> handleDeleteSelected());
    }

    private void setupRecyclerView() {
        adapter = new PhotoSectionAdapter();
        adapter.setOnPhotoClickListener((sharedView, photo) -> {
            if (!isSelectionMode) {
                Bundle args = new Bundle();
                args.putString("photo_id", photo.getId());
                Navigation.findNavController(requireView()).navigate(R.id.photoDetailFragment, args);
            }
        });
        adapter.setOnPhotoLongClickListener(photo -> {
            if (!isSelectionMode) {
                toggleSelectionMode(true);
            }
            adapter.toggleSelection(photo.getId());
        });

        binding.recyclerViewPhotos.setAdapter(adapter);
        applyLayoutManager();
    }

    private void applyLayoutManager() {
        final int spanCount = 3; // 3 cột cho giao diện kiểu Google Photos
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                int viewType = adapter.getItemViewType(position);
                return viewType == PhotoSectionAdapter.TYPE_HEADER ? spanCount : 1;
            }
        });
        binding.recyclerViewPhotos.setLayoutManager(layoutManager);

        if (adapter.isSelectionMode()) {
            toggleSelectionMode(false);
        }
        if (photoViewModel != null) {
            List<Photo> photos = photoViewModel.observeAllPhotos().getValue();
            if (photos != null) {
                handlePhotoList(photos);
            }
        }
    }

    private void handlePhotoList(List<Photo> photos) {
        if (photos == null) {
            photos = new ArrayList<>();
        }
        List<PhotoSection> sections = groupPhotos(photos, currentViewMode);
        adapter.setData(sections);
    }

    private List<PhotoSection> groupPhotos(List<Photo> photos, String mode) {
        List<PhotoSection> result = new ArrayList<>();
        if (mode.equals(VIEW_MODE_DAY)) {
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            String currentLabel = null;
            List<Photo> currentList = new ArrayList<>();
            for (Photo p : photos) {
                String label = dayFormat.format(new Date(p.getCreatedAt()));
                if (!label.equals(currentLabel)) {
                    if (!currentList.isEmpty()) {
                        result.add(new PhotoSection(currentLabel, new ArrayList<>(currentList)));
                        currentList.clear();
                    }
                    currentLabel = label;
                }
                currentList.add(p);
            }
            if (!currentList.isEmpty()) {
                result.add(new PhotoSection(currentLabel, currentList));
            }
        } else if (mode.equals(VIEW_MODE_MONTH)) {
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
            String currentLabel = null;
            List<Photo> currentList = new ArrayList<>();
            for (Photo p : photos) {
                String label = monthFormat.format(new Date(p.getCreatedAt()));
                if (!label.equals(currentLabel)) {
                    if (!currentList.isEmpty()) {
                        result.add(new PhotoSection(currentLabel, new ArrayList<>(currentList)));
                        currentList.clear();
                    }
                    currentLabel = label;
                }
                currentList.add(p);
            }
            if (!currentList.isEmpty()) {
                result.add(new PhotoSection(currentLabel, currentList));
            }
        } else {
            result.add(new PhotoSection(getString(R.string.all_photos), photos));
        }
        return result;
    }

    private void toggleSelectionMode(boolean enable) {
        isSelectionMode = enable;
        adapter.setSelectionMode(enable);

        requireActivity().invalidateOptionsMenu();

        if (enable) {
            binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_close);
            binding.toolbarPhotoList.setTitle("");
            binding.toolbarPhotoList.setNavigationOnClickListener(v -> toggleSelectionMode(false));
            binding.buttonDeleteSelected.setVisibility(View.VISIBLE);
            binding.checkBoxSelectAll.setVisibility(View.VISIBLE);
        } else {
            binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_more_vert);
            binding.toolbarPhotoList.setTitle(getString(R.string.photos));
            binding.toolbarPhotoList.setNavigationOnClickListener(this::showViewModePopup);
            binding.buttonDeleteSelected.setVisibility(View.GONE);
            binding.checkBoxSelectAll.setVisibility(View.GONE);
            binding.checkBoxSelectAll.setChecked(false);
        }
    }

    private void setupMenu() {
        MenuHost menuHost = requireActivity();
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menu.clear();
                menuInflater.inflate(R.menu.menu_photo_list, menu);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (!isSelectionMode) {
                    int itemId = menuItem.getItemId();
                    if (itemId == R.id.action_view_mode_comfortable) {
                        currentViewMode = VIEW_MODE_COMFORTABLE;
                        applyLayoutManager();
                        return true;
                    } else if (itemId == R.id.action_view_mode_day) {
                        currentViewMode = VIEW_MODE_DAY;
                        applyLayoutManager();
                        return true;
                    } else if (itemId == R.id.action_view_mode_month) {
                        currentViewMode = VIEW_MODE_MONTH;
                        applyLayoutManager();
                        return true;
                    }
                } else {
                    if (menuItem.getItemId() == R.id.action_delete_selected) {
                        handleDeleteSelected();
                        return true;
                    }
                }
                return false;
            }
        }, getViewLifecycleOwner());
    }

    private void handleDeleteSelected() {
        if (adapter.getSelectedPhotoIds().isEmpty()) {
            Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show();
        } else {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Photos")
                    .setMessage("Are you sure you want to delete " + adapter.getSelectedPhotoIds().size()
                            + " selected photos?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        for (String photoId : adapter.getSelectedPhotoIds()) {
                            photoViewModel.deletePhoto(photoId);
                        }
                        adapter.clearSelection();
                        toggleSelectionMode(false);
                        Toast.makeText(requireContext(), "Photos deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    private void showViewModePopup(View anchor) {
        if (isSelectionMode)
            return;
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_photo_list, popup.getMenu());

        // Hiển thị icon trong PopupMenu
        try {
            Field[] fields = popup.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceShowIcon = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceShowIcon.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        popup.setOnMenuItemClickListener(menuItem -> {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.action_view_mode_comfortable) {
                currentViewMode = VIEW_MODE_COMFORTABLE;
            } else if (itemId == R.id.action_view_mode_day) {
                currentViewMode = VIEW_MODE_DAY;
            } else if (itemId == R.id.action_view_mode_month) {
                currentViewMode = VIEW_MODE_MONTH;
            }
            applyLayoutManager();
            return true;
        });

        popup.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}