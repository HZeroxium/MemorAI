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
import java.util.Set;

/**
 * A RecyclerView adapter that can optionally be in "selectionMode".
 * When selectionMode = true, we show checkboxes and let user select multiple photos.
 */
public class PhotoAdapter extends ListAdapter<Photo, PhotoAdapter.PhotoViewHolder> {

    private OnPhotoLongClickListener onPhotoLongClickListener;
    private boolean selectionMode = false; // If true, show checkboxes

    private OnPhotoClickListener onPhotoClickListener;

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        final Photo photo = getItem(position);

        Glide.with(holder.itemView.getContext())
                .load(photo.getFilePath())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageViewPhoto);

        if (selectionMode) {
            holder.checkBox.setVisibility(View.VISIBLE);
            // Update checkbox based on whether photoId is in the selected set
            holder.checkBox.setChecked(selectedPhotoIds.contains(photo.getId()));
        } else {
            holder.checkBox.setVisibility(View.GONE);
        }

        // If we want normal click to open detail (only if not in selection mode)
        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                // Optional: If you want the entire item click to also toggle
                toggleSelection(photo.getId());
                notifyItemChanged(holder.getAdapterPosition());
            } else {
                if (onPhotoClickListener != null) {
                    onPhotoClickListener.onPhotoClick(holder.imageViewPhoto, photo);
                }
            }
        });

        // Long click can enable selection mode, if you want
        holder.itemView.setOnLongClickListener(v -> {
            if (onPhotoLongClickListener != null) {
                onPhotoLongClickListener.onPhotoLongClick(photo);
                return true;
            }
            return false;
        });

        // Crucial: handle the checkbox click directly
        holder.checkBox.setOnClickListener(v -> {
            if (holder.checkBox.isChecked()) {
                selectedPhotoIds.add(photo.getId());
            } else {
                selectedPhotoIds.remove(photo.getId());
            }
        });
    }

    /**
     * Called whenever we want to show/hide checkboxes for multi-select.
     */
    public void setSelectionMode(boolean enabled) {
        selectionMode = enabled;
        if (!enabled) {
            selectedPhotoIds.clear();
        }
        notifyDataSetChanged();
    }
    private final Set<String> selectedPhotoIds = new HashSet<>();

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

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(v);
    }

    /**
     * Return the set of currently selected photo IDs.
     */
    public Set<String> getSelectedPhotoIds() {
        return selectedPhotoIds;
    }

    /**
     * Let external code or the fragment keep the selection after data changes.
     * For example, if we re-bind data but want to preserve the user’s picks.
     */
    public void setSelectedPhotoIds(Set<String> selectedIds) {
        selectedPhotoIds.clear();
        selectedPhotoIds.addAll(selectedIds);
        notifyDataSetChanged();
    }

    /**
     * Toggle a photo in or out of the selection set.
     */
    public void toggleSelection(String photoId) {
        if (selectedPhotoIds.contains(photoId)) {
            selectedPhotoIds.remove(photoId);
        } else {
            selectedPhotoIds.add(photoId);
        }
    }

    /**
     * Clears all selection.
     */
    public void clearSelection() {
        selectedPhotoIds.clear();
        notifyDataSetChanged();
    }

    public boolean isSelected(String photoId) {
        return selectedPhotoIds.contains(photoId);
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(View sharedView, Photo photo);
    }

    public interface OnPhotoLongClickListener {
        void onPhotoLongClick(Photo photo);
    }

    public void setOnPhotoLongClickListener(OnPhotoLongClickListener listener) {
        this.onPhotoLongClickListener = listener;
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
