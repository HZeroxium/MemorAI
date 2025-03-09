package com.example.memorai.presentation.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.memorai.databinding.FragmentNotificationBinding;
import com.example.memorai.presentation.ui.activity.MainActivity;
import com.example.memorai.presentation.ui.adapter.NotificationAdapter;
import com.example.memorai.presentation.viewmodel.NotificationViewModel;

public class NotificationFragment extends Fragment {
    private FragmentNotificationBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        adapter = new NotificationAdapter();

        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewNotifications.setAdapter(adapter);

        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            adapter.setNotifications(notifications);
        });

        viewModel.loadNotifications();

        // ✅ Xử lý nút quay về
        binding.buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
