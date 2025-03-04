// presentation/ui/adapter/PhotoSectionAdapter.java
package com.example.memorai.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.domain.model.Photo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhotoSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_PHOTO = 1;

    // Our data: list of sections, each with a label and a list of photos
    private final List<PhotoSection> sectionList = new ArrayList<>();

    // Keep track of selected photo IDs
    private final Set<String> selectedIds = new HashSet<>();
    private boolean selectionMode = false;
    private OnPhotoClickListener onPhotoClickListener;
    private OnPhotoLongClickListener onPhotoLongClickListener;

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    public void setOnPhotoLongClickListener(OnPhotoLongClickListener listener) {
        this.onPhotoLongClickListener = listener;
    }

    public void setData(List<PhotoSection> sections) {
        this.sectionList.clear();
        this.sectionList.addAll(sections);
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public void setSelectionMode(boolean enabled) {
        this.selectionMode = enabled;
        if (!enabled) {
            selectedIds.clear();
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(String photoId) {
        if (selectedIds.contains(photoId)) {
            selectedIds.remove(photoId);
        } else {
            selectedIds.add(photoId);
        }
        notifyDataSetChanged();
    }

    public void selectAllInSection(int sectionIndex) {
        for (Photo p : sectionList.get(sectionIndex).getPhotos()) {
            selectedIds.add(p.getId());
        }
        notifyDataSetChanged();
    }

    public void clearSectionSelection(int sectionIndex) {
        for (Photo p : sectionList.get(sectionIndex).getPhotos()) {
            selectedIds.remove(p.getId());
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        // Determine if item is a header or a photo
        // We'll flatten the sections into a single list of "header or photo"
        // For each section: 1 header item + N photo items
        int running = 0;
        for (int i = 0; i < sectionList.size(); i++) {
            // If position == running => this is a header
            if (position == running) {
                return TYPE_HEADER;
            }
            running++;
            // If in the next range => it's a photo
            int size = sectionList.get(i).getPhotos().size();
            if (position < running + size) {
                return TYPE_PHOTO;
            }
            running += size;
        }
        return TYPE_PHOTO; // fallback
    }

    @Override
    public int getItemCount() {
        // 1 header per section + sum of all photos
        int total = 0;
        for (PhotoSection sec : sectionList) {
            total += 1; // header
            total += sec.getPhotos().size();
        }
        return total;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo_section_header, parent, false);
            return new HeaderViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int[] index = findSectionAndOffset(position);
        int sectionIndex = index[0];
        int offset = index[1]; // -1 means it's a header

        if (offset < 0) {
            // This is a header
            HeaderViewHolder hvh = (HeaderViewHolder) holder;
            PhotoSection section = sectionList.get(sectionIndex);
            hvh.bind(section.getLabel(), sectionIndex);
        } else {
            // This is a photo
            PhotoViewHolder pvh = (PhotoViewHolder) holder;
            Photo photo = sectionList.get(sectionIndex).getPhotos().get(offset);
            pvh.bind(photo);
        }
    }

    // Find which section and offset the position belongs to
    private int[] findSectionAndOffset(int position) {
        int running = 0;
        for (int i = 0; i < sectionList.size(); i++) {
            // If position == running => header
            if (position == running) {
                return new int[]{i, -1};
            }
            running++;
            int size = sectionList.get(i).getPhotos().size();
            if (position < running + size) {
                int offset = position - running;
                return new int[]{i, offset};
            }
            running += size;
        }
        return new int[]{0, -1};
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(View sharedView, Photo photo);
    }

    public interface OnPhotoLongClickListener {
        void onPhotoLongClick(Photo photo);
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLabel;
        CheckBox checkBoxSelectAll;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLabel = itemView.findViewById(R.id.textViewSectionLabel);
            checkBoxSelectAll = itemView.findViewById(R.id.checkBoxSectionSelectAll);
        }

        void bind(String label, int sectionIndex) {
            textViewLabel.setText(label);

            // If selection mode is active, show the section's "Select All" checkbox
            if (selectionMode) {
                checkBoxSelectAll.setVisibility(View.VISIBLE);
                // If all photos in this section are selected, check the box
                boolean allSelected = true;
                for (Photo p : sectionList.get(sectionIndex).getPhotos()) {
                    if (!selectedIds.contains(p.getId())) {
                        allSelected = false;
                        break;
                    }
                }
                checkBoxSelectAll.setChecked(allSelected);

                checkBoxSelectAll.setOnCheckedChangeListener((btn, isChecked) -> {
                    if (isChecked) {
                        selectAllInSection(sectionIndex);
                    } else {
                        clearSectionSelection(sectionIndex);
                    }
                });
            } else {
                checkBoxSelectAll.setVisibility(View.GONE);
            }
        }
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPhoto;
        CheckBox checkBoxItemSelect;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
            checkBoxItemSelect = itemView.findViewById(R.id.checkBoxItemSelect);

            itemView.setOnClickListener(v -> {
                int[] index = findSectionAndOffset(getBindingAdapterPosition());
                if (index[1] < 0) return; // header
                Photo photo = sectionList.get(index[0]).getPhotos().get(index[1]);
                if (selectionMode) {
                    toggleSelection(photo.getId());
                } else {
                    if (onPhotoClickListener != null) {
                        onPhotoClickListener.onPhotoClick(imageViewPhoto, photo);
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (onPhotoLongClickListener != null) {
                    int[] index = findSectionAndOffset(getBindingAdapterPosition());
                    if (index[1] >= 0) {
                        Photo photo = sectionList.get(index[0]).getPhotos().get(index[1]);
                        onPhotoLongClickListener.onPhotoLongClick(photo);
                    }
                    return true;
                }
                return false;
            });
        }

        void bind(Photo photo) {
            Glide.with(itemView.getContext())
                    .load(photo.getFilePath())
                    .placeholder(R.drawable.placeholder_image)
                    .into(imageViewPhoto);

            if (selectionMode) {
                checkBoxItemSelect.setVisibility(View.VISIBLE);
                checkBoxItemSelect.setChecked(selectedIds.contains(photo.getId()));
            } else {
                checkBoxItemSelect.setVisibility(View.GONE);
            }
        }
    }
}
