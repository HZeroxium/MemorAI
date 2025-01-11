package com.example.memorai.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.domain.model.Album;

public class AlbumAdapter extends ListAdapter<Album, AlbumAdapter.AlbumViewHolder> {

    public AlbumAdapter() {
        super(new DiffUtil.ItemCallback<Album>() {
            @Override
            public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                return oldItem.getTitle().equals(newItem.getTitle());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                return oldItem.equals(newItem);
            }
        });
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = getItem(position);
        holder.bind(album);
    }

    static class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTitle;
        private final ImageView ivThumbnail;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_album_title);
            ivThumbnail = itemView.findViewById(R.id.iv_album_thumbnail);
        }

        void bind(Album album) {
            tvTitle.setText(album.getTitle());
            Glide.with(ivThumbnail.getContext()).load(album.getThumbnailUrl()).into(ivThumbnail);
        }
    }
}
