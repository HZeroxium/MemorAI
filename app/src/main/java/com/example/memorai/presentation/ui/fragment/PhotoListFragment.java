// presentation/ui/fragment/PhotoListFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoListFragment extends Fragment {

    private FragmentPhotoListBinding binding;
    private PhotoViewModel photoViewModel;
    private static final String VIEW_MODE_COMFORTABLE = "COMFORTABLE";
    private static final String VIEW_MODE_DAY = "DAY";
    private static final String VIEW_MODE_MONTH = "MONTH";
    private PhotoSectionAdapter adapter;
    private boolean isSelectionMode = false;
    private String currentViewMode = "COMFORTABLE";

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

        // Initial toolbar setup
        binding.toolbarPhotoList.setTitle("MemorAI");
//        binding.toolbarPhotoList.inflateMenu(R.menu.menu_photo_list);
        binding.toolbarPhotoList.setOnMenuItemClickListener(this::onToolbarMenuItemClick);

        // Navigation icon will be used to open the popup for layout modes
        binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_more_vert);
        binding.toolbarPhotoList.setNavigationOnClickListener(this::showViewModePopup);

        // "Select All" global is hidden initially
        binding.checkBoxSelectAll.setVisibility(View.GONE);
        binding.checkBoxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isSelectionMode) return;
            // If you want a global "Select All" for the entire library, do:
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isChecked) {
                    adapter.selectAllInSection(0); //
                } else {
                    adapter.clearSectionSelection(0);
                }
            });
        });

        // Setup RecyclerView
        adapter = new PhotoSectionAdapter();
        adapter.setOnPhotoClickListener((sharedView, photo) -> {
            if (!isSelectionMode) {
                // Open detail
                Bundle args = new Bundle();
                args.putString("photo_url", photo.getFilePath());
                Navigation.findNavController(view).navigate(R.id.photoDetailFragment, args);
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

        // ViewModel
        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), this::handlePhotoList);
        photoViewModel.loadAllPhotos();
    }

    private boolean onToolbarMenuItemClick(MenuItem item) {
        // If you have additional toolbar items
        showViewModePopup(binding.toolbarPhotoList);
        return true;
    }

    private void showViewModePopup(View anchor) {
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_photo_list, popup.getMenu());
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

    private void applyLayoutManager() {
        switch (currentViewMode) {
            case VIEW_MODE_DAY:
                // e.g. 2 columns
                binding.recyclerViewPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
                break;
            case VIEW_MODE_MONTH:
                // e.g. 3 columns
                binding.recyclerViewPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 3));
                break;
            case VIEW_MODE_COMFORTABLE:
            default:
                // e.g. 1 column
                binding.recyclerViewPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 1));
                break;
        }
        // If you also want different grouping logic (day vs. month),
        // re-group the existing photos after changing the mode.
        if (adapter != null && adapter.isSelectionMode()) {
            toggleSelectionMode(false);
        }
        // If you have the data, re-group them
        if (photoViewModel != null && photoViewModel.observeAllPhotos().getValue() != null) {
            handlePhotoList(photoViewModel.observeAllPhotos().getValue());
        }
    }

    private void handlePhotoList(List<Photo> photos) {
        // Group photos by day or month
        List<PhotoSection> sections = groupPhotos(photos, currentViewMode);
        adapter.setData(sections);
    }

    private List<PhotoSection> groupPhotos(List<Photo> photos, String mode) {
        // Simple example: group by "day" or "month" using date/time
        // Real logic would parse photo timestamps, etc.

        // Sort photos by date ascending, then chunk them
        // For brevity, assume they're already sorted

        List<PhotoSection> result = new ArrayList<>();
        if (mode.equals(VIEW_MODE_DAY)) {
            // Group by day
            // For each photo, get day = format "yyyy-MM-dd"
            String currentLabel = null;
            List<Photo> currentList = new ArrayList<>();
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");

            for (Photo p : photos) {
                String label = dayFormat.format(new Date(p.getCreatedAt()));
                if (!label.equals(currentLabel)) {
                    // close old group
                    if (currentList.size() > 0) {
                        result.add(new PhotoSection(currentLabel, new ArrayList<>(currentList)));
                        currentList.clear();
                    }
                    currentLabel = label;
                }
                currentList.add(p);
            }
            if (currentList.size() > 0) {
                result.add(new PhotoSection(currentLabel, currentList));
            }
        } else if (mode.equals(VIEW_MODE_MONTH)) {
            // Group by "yyyy-MM"
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
            String currentLabel = null;
            List<Photo> currentList = new ArrayList<>();

            for (Photo p : photos) {
                String label = monthFormat.format(new Date(p.getCreatedAt()));
                if (!label.equals(currentLabel)) {
                    if (currentList.size() > 0) {
                        result.add(new PhotoSection(currentLabel, new ArrayList<>(currentList)));
                        currentList.clear();
                    }
                    currentLabel = label;
                }
                currentList.add(p);
            }
            if (currentList.size() > 0) {
                result.add(new PhotoSection(currentLabel, currentList));
            }
        } else {
            // Comfortable => single group
            result.add(new PhotoSection("All Photos", photos));
        }

        return result;
    }

    private void toggleSelectionMode(boolean enable) {
        isSelectionMode = enable;
        adapter.setSelectionMode(enable);

        if (enable) {
            // Switch the toolbar to "Close" style
            binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_close);
            binding.toolbarPhotoList.setTitle("Select photos");

            binding.toolbarPhotoList.setNavigationOnClickListener(v -> toggleSelectionMode(false));

            binding.checkBoxSelectAll.setVisibility(View.GONE);
        } else {
            // Restore normal toolbar
            binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_more_vert);
            binding.toolbarPhotoList.setNavigationOnClickListener(this::showViewModePopup);
            binding.toolbarPhotoList.setTitle("MemorAI");
            binding.checkBoxSelectAll.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
