// presentation/ui/adapter/SelectedPhotoAdapter.java
package com.example.memorai.presentation.ui.adapter;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.domain.model.Photo;

public class SelectedPhotoAdapter extends ListAdapter<Photo, SelectedPhotoAdapter.ViewHolder> {

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
    private OnRemoveClickListener removeClickListener;

    public SelectedPhotoAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnRemoveClickListener(OnRemoveClickListener listener) {
        this.removeClickListener = listener;
        notifyDataSetChanged(); // Ensure UI refresh
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_selected_photo, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Photo photo = getItem(position);

        Bitmap bitmap = photo.getBitmap(); // Giả sử Photo có getBitmap()
        if (bitmap != null) {
            Glide.with(holder.itemView.getContext())
                    .load(bitmap)
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.imageViewSelected);
        } else {
            holder.imageViewSelected.setImageResource(R.drawable.placeholder_image);
        }
        holder.buttonRemovePhoto.setOnClickListener(v -> {
            if (removeClickListener != null) {
                removeClickListener.onRemoveClick(photo);
            }
        });
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(Photo photo);
    }

    static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        ImageView imageViewSelected;
        ImageButton buttonRemovePhoto;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewSelected = itemView.findViewById(R.id.imageViewSelected);
            buttonRemovePhoto = itemView.findViewById(R.id.buttonRemovePhoto);
        }
    }
}
