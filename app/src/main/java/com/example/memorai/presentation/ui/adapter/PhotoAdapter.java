// presentation/ui/adapter/PhotoAdapter.java
package com.example.memorai.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.domain.model.Photo;

public class PhotoAdapter extends ListAdapter<Photo, PhotoAdapter.PhotoViewHolder> {
    private OnPhotoClickListener onPhotoClickListener;

    private static final DiffUtil.ItemCallback<Photo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Photo>() {
                @Override
                public boolean areItemsTheSame(@NonNull Photo oldItem, @NonNull Photo newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Photo oldItem, @NonNull Photo newItem) {
                    return oldItem.getUrl().equals(newItem.getUrl())
                            && oldItem.getAlbumId() == newItem.getAlbumId()
                            && oldItem.getCreatedAt() == newItem.getCreatedAt();
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

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = getItem(position);
        Glide.with(holder.itemView.getContext())
                .load(photo.getUrl())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.imageViewPhoto);

        holder.itemView.setOnClickListener(v -> {
            if (onPhotoClickListener != null) {
                onPhotoClickListener.onPhotoClick(holder.imageViewPhoto, photo);
            }
        });
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(View sharedView, Photo photo);
    }

    static class PhotoViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        ImageView imageViewPhoto;

        PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
        }
    }
}
