// presentation/ui/fragment/PhotoListFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

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
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize toolbar with no menu; we will handle menu via MenuHost
        binding.toolbarPhotoList.setTitle(getString(R.string.photos));
        binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_more_vert);
        binding.toolbarPhotoList.setNavigationOnClickListener(this::showViewModePopup);

        // Hide "Select All" by default
        binding.checkBoxSelectAll.setVisibility(View.GONE);
        binding.checkBoxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isSelectionMode) return;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isChecked) {
                    // Example: select all photos in the first section
                    adapter.selectAllInSection(0);
                } else {
                    adapter.clearSectionSelection(0);
                }
            });
        });

        // Setup RecyclerView
        adapter = new PhotoSectionAdapter();
        adapter.setOnPhotoClickListener((sharedView, photo) -> {
            if (!isSelectionMode) {
                // Pass photoId & photoUrl for the detail
                Bundle args = new Bundle();
                args.putString("photo_id", photo.getId());
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

        // Setup PhotoViewModel
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), this::handlePhotoList);
        photoViewModel.loadAllPhotos();

        // Use MenuHost & MenuProvider to manage toolbar menu
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
                    // Normal mode items
                    if (menuItem.getItemId() == R.id.action_view_mode_comfortable) {
                        currentViewMode = VIEW_MODE_COMFORTABLE;
                        applyLayoutManager();
                        return true;
                    } else if (menuItem.getItemId() == R.id.action_view_mode_day) {
                        currentViewMode = VIEW_MODE_DAY;
                        applyLayoutManager();
                        return true;
                    } else if (menuItem.getItemId() == R.id.action_view_mode_month) {
                        currentViewMode = VIEW_MODE_MONTH;
                        applyLayoutManager();
                        return true;
                    }
                } else {
                    // Selection mode items
                    if (menuItem.getItemId() == R.id.action_delete_selected) {
                        if (adapter.getSelectedPhotoIds().isEmpty()) {
                            Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show();
                        } else {
                            new AlertDialog.Builder(requireContext())
                                    .setTitle("Delete Photos")
                                    .setMessage("Are you sure you want to delete " + adapter.getSelectedPhotoIds().size() + " selected photos?")
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
                        return true;
                    }
                }
                return false;
            }
        }, getViewLifecycleOwner());

        // Handle click on the delete button
        binding.buttonDeleteSelected.setOnClickListener(v -> {
            if (adapter.getSelectedPhotoIds().isEmpty()) {
                Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Photos")
                        .setMessage("Are you sure you want to delete the selected photos?")
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
        });

    }

    private void applyLayoutManager() {
        // We'll default to 3 columns for a Google Photos style (or 2 if you prefer)
        final int spanCount = 3;

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), spanCount);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                // If item is a section header, take the full row
                int viewType = adapter.getItemViewType(position);
                if (viewType == PhotoSectionAdapter.TYPE_HEADER) {
                    return spanCount; // header spans all columns
                } else {
                    return 1; // photo items occupy 1 column each
                }
            }
        });
        binding.recyclerViewPhotos.setLayoutManager(layoutManager);

        // Hide selection mode if user changed layout
        if (adapter.isSelectionMode()) {
            toggleSelectionMode(false);
        }
        if (photoViewModel != null && photoViewModel.observeAllPhotos().getValue() != null) {
            handlePhotoList(photoViewModel.observeAllPhotos().getValue());
        }
    }


    private void handlePhotoList(List<Photo> photos) {
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

        requireActivity().invalidateOptionsMenu(); // Refresh menu options

        ExtendedFloatingActionButton fab = binding.buttonDeleteSelected; // from your new layout

        if (enable) {
            // Switch toolbar to selection mode
            binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_close);
            binding.toolbarPhotoList.setTitle("Select photos");
            binding.toolbarPhotoList.setNavigationOnClickListener(v -> toggleSelectionMode(false));

            // Show the Extended FAB
            fab.setVisibility(View.VISIBLE);
            fab.setOnClickListener(v -> {
                if (adapter.getSelectedPhotoIds().isEmpty()) {
                    Toast.makeText(requireContext(), "No photos selected", Toast.LENGTH_SHORT).show();
                } else {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Photos")
                            .setMessage("Are you sure you want to delete " + adapter.getSelectedPhotoIds().size() + " selected photos?")
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
            });

        } else {
            // Normal mode
            binding.toolbarPhotoList.setNavigationIcon(R.drawable.ic_more_vert);
            binding.toolbarPhotoList.setTitle(getString(R.string.photos));
            binding.toolbarPhotoList.setNavigationOnClickListener(this::showViewModePopup);

            fab.setVisibility(View.GONE);
        }
    }


    private void showViewModePopup(View anchor) {
        if (isSelectionMode) return; // Ignore if in selection mode
        PopupMenu popup = new PopupMenu(requireContext(), anchor);
        popup.getMenuInflater().inflate(R.menu.menu_photo_list, popup.getMenu());

        // Force icons to be visible in PopupMenu
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
