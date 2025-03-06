// presentation/ui/adapter/AlbumAdapter.java
package com.example.memorai.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.domain.model.Album;

public class AlbumAdapter extends ListAdapter<Album, AlbumAdapter.AlbumViewHolder> {
    private OnAlbumClickListener onAlbumClickListener;

    private static final DiffUtil.ItemCallback<Album> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Album>() {
                @Override
                public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return oldItem.getId().equals(newItem.getId());
                }
                @Override
                public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return oldItem.equals(newItem);
                }
            };

    public AlbumAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = getItem(position);
        holder.textViewAlbumName.setText(album.getName());

        // Load album cover with Glide and add a placeholder
        Glide.with(holder.itemView.getContext())
                .load(album.getCoverPhotoUrl())
                .placeholder(R.drawable.placeholder_image)
                .into(holder.imageViewCover);

        holder.itemView.setOnClickListener(v -> {
            if (onAlbumClickListener != null) {
                onAlbumClickListener.onAlbumClick(album);
            }
        });
    }

    public void setOnAlbumClickListener(OnAlbumClickListener listener) {
        this.onAlbumClickListener = listener;
    }

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    static class AlbumViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView textViewAlbumName;
        ImageView imageViewCover;
        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAlbumName = itemView.findViewById(R.id.textViewAlbumName);
            imageViewCover = itemView.findViewById(R.id.imageViewAlbumCover);
        }
    }
}
