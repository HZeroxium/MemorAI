// presentation/ui/adapter/PhotoSectionAdapter.java
package com.example.memorai.presentation.ui.adapter;

import android.graphics.Bitmap;
import android.util.Log;
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

/**
 * Adapter for grouping photos into sections (e.g., by day or month).
 * Supports selection mode for multiple-photo selection.
 */
public class PhotoSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_HEADER = 0;
    private static final int TYPE_PHOTO = 1;

    private final List<PhotoSection> sectionList = new ArrayList<>();
    private final Set<String> selectedIds = new HashSet<>();
    private boolean selectionMode = false;

    // Public methods to manipulate data
    public void setData(List<PhotoSection> sections) {
        sectionList.clear();
        sectionList.addAll(sections);
        notifyDataSetChanged();
    }

    public void setSelectionMode(boolean enabled) {
        selectionMode = enabled;
        if (!enabled) {
            selectedIds.clear();
        }
        notifyDataSetChanged();
    }

    private OnPhotoClickListener onPhotoClickListener;
    private OnPhotoLongClickListener onPhotoLongClickListener;

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }
    public void setOnPhotoLongClickListener(OnPhotoLongClickListener listener) {
        this.onPhotoLongClickListener = listener;
    }

    public void clearSelection() {
        selectedIds.clear();
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public Set<String> getSelectedPhotoIds() {
        return new HashSet<>(selectedIds);
    }

    public void toggleSelection(String photoId) {
        if (selectedIds.contains(photoId)) {
            selectedIds.remove(photoId);
        } else {
            selectedIds.add(photoId);
        }
        notifyDataSetChanged();
    }

    // Flatten sections: each section has 1 header + N photos
    @Override
    public int getItemViewType(int position) {
        int running = 0;
        for (int i = 0; i < sectionList.size(); i++) {
            if (position == running) {
                return TYPE_HEADER;
            }
            running++;
            int size = sectionList.get(i).getPhotos().size();
            if (position < running + size) {
                return TYPE_PHOTO;
            }
            running += size;
        }
        return TYPE_PHOTO;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int[] index = findSectionAndOffset(position);
        int sectionIndex = index[0];
        int offset = index[1]; // offset < 0 => header

        if (offset < 0) {
            // It's a header
            HeaderViewHolder hvh = (HeaderViewHolder) holder;
            PhotoSection section = sectionList.get(sectionIndex);
            hvh.bind(section.getLabel(), sectionIndex);
        } else {
            // It's a photo
            PhotoViewHolder pvh = (PhotoViewHolder) holder;
            Photo photo = sectionList.get(sectionIndex).getPhotos().get(offset);
            pvh.bind(photo);
        }
    }

    public void selectAllInSection(int sectionIndex) {
        List<Photo> photos = sectionList.get(sectionIndex).getPhotos();
        int startPosition = findFirstPositionOfSection(sectionIndex);

        // Thêm tất cả ảnh vào selectedIds
        for (Photo p : photos) {
            selectedIds.add(p.getId());
        }

        // Chỉ cập nhật các item trong section này
        notifyItemRangeChanged(startPosition + 1, photos.size()); // +1 để bỏ qua header
    }

    public void clearSectionSelection(int sectionIndex) {
        List<Photo> photos = sectionList.get(sectionIndex).getPhotos();
        int startPosition = findFirstPositionOfSection(sectionIndex);

        // Xóa tất cả ảnh khỏi selectedIds
        for (Photo p : photos) {
            selectedIds.remove(p.getId());
        }

        // Chỉ cập nhật các item trong section này
        notifyItemRangeChanged(startPosition + 1, photos.size());
    }

    private int findFirstPositionOfSection(int sectionIndex) {
        int position = 0;
        for (int i = 0; i < sectionIndex; i++) {
            position += 1 + sectionList.get(i).getPhotos().size(); // header + photos
        }
        return position;
    }
    private int[] findSectionAndOffset(int position) {
        int running = 0;
        for (int i = 0; i < sectionList.size(); i++) {
            if (position == running) {
                return new int[]{i, -1}; // header
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

    @Override
    public int getItemCount() {
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

    // Callbacks
    public interface OnPhotoClickListener {
        void onPhotoClick(View sharedView, Photo photo);
    }

    public interface OnPhotoLongClickListener {
        void onPhotoLongClick(Photo photo);
    }

    // ViewHolders

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

            if (selectionMode) {
                checkBoxSelectAll.setVisibility(View.VISIBLE);
                // Tạm thời gỡ listener để tránh kích hoạt khi setChecked
                checkBoxSelectAll.setOnCheckedChangeListener(null);

                // Kiểm tra trạng thái chọn
                boolean allSelected = true;
                for (Photo p : sectionList.get(sectionIndex).getPhotos()) {
                    if (!selectedIds.contains(p.getId())) {
                        allSelected = false;
                        break;
                    }
                }
                checkBoxSelectAll.setChecked(allSelected);

                // Thiết lập listener sau khi đã setChecked
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
                if (index[1] < 0) return; // ignore header
                Photo photo = sectionList.get(index[0]).getPhotos().get(index[1]);
                if (selectionMode) {
                    toggleSelection(photo.getId());
                } else if (onPhotoClickListener != null) {
                    onPhotoClickListener.onPhotoClick(imageViewPhoto, photo);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int[] index = findSectionAndOffset(getBindingAdapterPosition());
                if (index[1] >= 0 && onPhotoLongClickListener != null) {
                    Photo photo = sectionList.get(index[0]).getPhotos().get(index[1]);
                    onPhotoLongClickListener.onPhotoLongClick(photo);
                    return true;
                }
                return false;
            });
        }

        void bind(Photo photo) {
            Bitmap bitmap = photo.getBitmap(); // Giả sử Photo có getBitmap()
            if (bitmap != null) {
                Glide.with(itemView.getContext())
                        .load(bitmap)
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageViewPhoto);
            } else {
                imageViewPhoto.setImageResource(R.drawable.placeholder_image);
            }

            if (selectionMode) {
                checkBoxItemSelect.setVisibility(View.VISIBLE);
                checkBoxItemSelect.setChecked(selectedIds.contains(photo.getId()));
            } else {
                checkBoxItemSelect.setVisibility(View.GONE);
            }
        }

    }
}

