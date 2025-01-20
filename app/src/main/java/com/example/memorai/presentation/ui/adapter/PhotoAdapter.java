package com.example.memorai.presentation.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.domain.model.Photo;

import java.util.List;

/**
 * Adapter hiển thị danh sách ảnh.
 * Lưu ý: Để tối ưu cho dữ liệu lớn, có thể cân nhắc ListAdapter + DiffUtil.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final Context context;
    private final List<Photo> photos;
    private OnPhotoClickListener onPhotoClickListener;

    public PhotoAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }

    public void setOnPhotoClickListener(OnPhotoClickListener listener) {
        this.onPhotoClickListener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);

        // Dùng Glide để tải ảnh
        Glide.with(context)
                .load(photo.getUrl())
                .placeholder(R.drawable.placeholder_image) // ảnh giả trong lúc load
                .error(R.drawable.error_image)             // ảnh hiển thị khi load thất bại
                .into(holder.imageViewPhoto);

        // Xử lý sự kiện click
        holder.itemView.setOnClickListener(v -> {
            if (onPhotoClickListener != null) {
                onPhotoClickListener.onPhotoClick(photo, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    public interface OnPhotoClickListener {
        void onPhotoClick(Photo photo, int position);
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPhoto;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewPhoto = itemView.findViewById(R.id.imageViewPhoto);
        }
    }
}
