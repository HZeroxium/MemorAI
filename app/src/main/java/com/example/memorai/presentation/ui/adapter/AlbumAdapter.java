// presentation/ui/adapter/AlbumAdapter.java
package com.example.memorai.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.example.memorai.R;
import com.example.memorai.domain.model.Album;

public class AlbumAdapter extends ListAdapter<Album, AlbumAdapter.AlbumViewHolder> {
    private static final DiffUtil.ItemCallback<Album> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Album>() {
                @Override
                public boolean areItemsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Album oldItem, @NonNull Album newItem) {
                    return oldItem.getName().equals(newItem.getName())
                            && oldItem.getCreatedAt() == newItem.getCreatedAt();
                }
            };
    private OnAlbumClickListener onAlbumClickListener;

    public AlbumAdapter() {
        super(DIFF_CALLBACK);
    }

    public void setOnAlbumClickListener(OnAlbumClickListener listener) {
        this.onAlbumClickListener = listener;
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
        holder.itemView.setOnClickListener(v -> {
            if (onAlbumClickListener != null) onAlbumClickListener.onAlbumClick(album);
        });
    }

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
    }

    static class AlbumViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        TextView textViewAlbumName;

        AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAlbumName = itemView.findViewById(R.id.textViewAlbumName);
        }
    }
}

