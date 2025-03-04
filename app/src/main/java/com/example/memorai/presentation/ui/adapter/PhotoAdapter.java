// presentation/ui/adapter/PhotoAdapter.java
package com.example.memorai.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.domain.model.Photo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhotoAdapter extends ListAdapter<Photo, PhotoAdapter.PhotoViewHolder> {
    private OnPhotoClickListener onPhotoClickListener;

    // A set of selected photo IDs
    private final Set<String> selectedPhotoIds = new HashSet<>();
    // Whether we are in "selection mode" or not
    private boolean selectionMode = false;

    private static final DiffUtil.ItemCallback<Photo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Photo>() {
                @Override
                public boolean areItemsTheSame(@NonNull Photo oldItem, @NonNull Photo newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Photo oldItem, @NonNull Photo newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public PhotoAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(v);
    }
    private OnPhotoLongClickListener onPhotoLongClickListener;

    public interface OnPhotoClickListener {
        void onPhotoClick(View sharedView, Photo photo);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = getItem(position);
        // Load image
        Glide.with(holder.itemView.getContext())
                .load(photo.getFilePath())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageViewPhoto);

        // If selectionMode is active, show a "selected" overlay or highlight
        if (selectionMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.checkBox.setChecked(selectedPhotoIds.contains(photo.getId()));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(photo.getId());
            } else {
                // Normal click -> open detail
                if (onPhotoClickListener != null) {
                    onPhotoClickListener.onPhotoClick(holder.imageViewPhoto, photo);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onPhotoLongClickListener != null) {
                onPhotoLongClickListener.onPhotoLongClick(photo);
                return true;
            }
            return false;
        });
    }

    public void setSelectionMode(boolean enabled) {
        selectionMode = enabled;
        if (!enabled) {
            selectedPhotoIds.clear();
        }
        notifyDataSetChanged();
    }

    public void toggleSelection(String photoId) {
        if (selectedPhotoIds.contains(photoId)) {
            selectedPhotoIds.remove(photoId);
        } else {
            selectedPhotoIds.add(photoId);
        }
        notifyDataSetChanged();
    }

    public void selectAll(List<Photo> data) {
        selectedPhotoIds.clear();
        for (Photo p : data) {
            selectedPhotoIds.add(p.getId());
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPhotoIds.clear();
        notifyDataSetChanged();
    }

    public Set<String> getSelectedPhotoIds() {
        return selectedPhotoIds;
    }

    public void setOnPhotoLongClickListener(OnPhotoLongClickListener listener) {
        this.onPhotoLongClickListener = listener;
    }

    public interface OnPhotoLongClickListener {
        void onPhotoLongClick(Photo photo);
    }

    static class PhotoViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        ImageView imageViewPhoto;
        CheckBox checkBox;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
            checkBox = itemView.findViewById(R.id.checkBoxItemSelect);
        }
    }

}
