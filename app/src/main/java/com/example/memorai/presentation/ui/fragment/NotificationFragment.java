package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorai.R;
import com.example.memorai.domain.model.Notification;
import com.example.memorai.presentation.ui.adapter.NotificationAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    private NotificationAdapter adapter;
    private List<Notification> notificationList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewNotifications);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(adapter);

        DatabaseReference notificationsRef = FirebaseDatabase.getInstance().getReference("notifications");

        notificationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    // Lấy ID Firebase nếu cần
                    String notificationId = dataSnapshot.getKey();
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    if (notification != null) {
                        notificationList.add(notification);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("NotificationFragment", "Failed to load notifications.", error.toException());
            }
        });

        View buttonBack = view.findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        return view;
    }
}
