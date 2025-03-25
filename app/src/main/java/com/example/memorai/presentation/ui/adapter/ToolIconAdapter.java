package com.example.memorai.presentation.ui.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorai.R;
import com.example.memorai.domain.model.ToolIcon;

import java.util.List;

public class ToolIconAdapter extends RecyclerView.Adapter<ToolIconAdapter.ToolViewHolder> {

    private final List<ToolIcon> toolList;
    private final OnToolClickListener listener;

    public interface OnToolClickListener {
        void onToolClick(ToolIcon tool);
    }

    public ToolIconAdapter(List<ToolIcon> toolList, OnToolClickListener listener) {
        this.toolList = toolList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ToolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_tools, parent, false);
        return new ToolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ToolViewHolder holder, int position) {
        ToolIcon tool = toolList.get(position);
        holder.imgToolIcon.setImageResource(tool.getIconResId());
        holder.txtToolName.setText(tool.getToolName());

        holder.itemView.setOnClickListener(v -> listener.onToolClick(tool));
    }

    @Override
    public int getItemCount() {
        return toolList.size();
    }

    public static class ToolViewHolder extends RecyclerView.ViewHolder {
        ImageView imgToolIcon;
        TextView txtToolName;

        public ToolViewHolder(@NonNull View itemView) {
            super(itemView);
            imgToolIcon = itemView.findViewById(R.id.imgToolIcon);
            txtToolName = itemView.findViewById(R.id.txtToolName);
        }
    }
}
