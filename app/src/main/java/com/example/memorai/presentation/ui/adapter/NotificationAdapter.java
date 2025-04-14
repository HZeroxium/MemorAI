package com.example.memorai.presentation.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorai.R;
import com.example.memorai.domain.model.Notification;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationAdapter extends ListAdapter<Notification, NotificationAdapter.NotificationViewHolder> {
    private OnNotificationClickListener onNotificationClickListener;

    public NotificationAdapter() {
        super(DIFF_CALLBACK);
    }

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
        void onNotificationDelete(Notification notification);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.onNotificationClickListener = listener;
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = getItem(position);
        holder.bind(notification);
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView messageTextView;
        private final TextView timestampTextView;
        private final View deleteButton;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.textViewNotificationTitle);
            messageTextView = itemView.findViewById(R.id.textViewNotificationMessage);
            timestampTextView = itemView.findViewById(R.id.textViewNotificationTimestamp);
            deleteButton = itemView.findViewById(R.id.buttonDeleteNotification);

            itemView.setOnClickListener(v -> {
                if (onNotificationClickListener != null) {
                    onNotificationClickListener.onNotificationClick(getItem(getAdapterPosition()));
                }
            });

            deleteButton.setOnClickListener(v -> {
                if (onNotificationClickListener != null) {
                    onNotificationClickListener.onNotificationDelete(getItem(getAdapterPosition()));
                }
            });
        }

        public void bind(Notification notification) {
            titleTextView.setText(notification.getTitle());
            messageTextView.setText(notification.getMessage());
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
            timestampTextView.setText(sdf.format(new Date(notification.getTimestamp())));
        }
    }

    private static final DiffUtil.ItemCallback<Notification> DIFF_CALLBACK = new DiffUtil.ItemCallback<Notification>() {
        @Override
        public boolean areItemsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Notification oldItem, @NonNull Notification newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getMessage().equals(newItem.getMessage()) &&
                    oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };
}