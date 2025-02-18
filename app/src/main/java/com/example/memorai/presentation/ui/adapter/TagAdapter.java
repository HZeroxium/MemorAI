//// presentation/ui/adapter/TagAdapter.java
//package com.example.memorai.presentation.ui.adapter;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.memorai.R;
//import java.util.List;
//
//public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
//    private List<String> tags;
//
//    public void setTags(List<String> tags) {
//        this.tags = tags;
//        notifyDataSetChanged();
//    }
//
//    @NonNull
//    @Override
//    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tag, parent, false);
//        return new TagViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
//        String tag = tags.get(position);
//        holder.textViewTag.setText(tag);
//    }
//
//    @Override
//    public int getItemCount() {
//        return tags != null ? tags.size() : 0;
//    }
//
//    static class TagViewHolder extends RecyclerView.ViewHolder {
//        TextView textViewTag;
//
//        TagViewHolder(@NonNull View itemView) {
//            super(itemView);
//            textViewTag = itemView.findViewById(R.id.textViewTag);
//        }
//    }
//}
