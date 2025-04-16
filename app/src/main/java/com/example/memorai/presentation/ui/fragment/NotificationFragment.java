package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentNotificationBinding;
import com.example.memorai.domain.model.Notification;
import com.example.memorai.presentation.ui.adapter.NotificationAdapter;
import com.example.memorai.presentation.viewmodel.NotificationViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class NotificationFragment extends Fragment {
    private FragmentNotificationBinding binding;
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(NotificationViewModel.class);
        setupToolbar();
        setupRecyclerView();
        observeViewModel();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> {
            Navigation.findNavController(requireView()).popBackStack();
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter();
        binding.recyclerViewNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewNotifications.setAdapter(adapter);

        adapter.setOnNotificationClickListener(new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(Notification notification) {
                Toast.makeText(requireContext(), getString(R.string.notification_clicked,notification.getTitle()) , Toast.LENGTH_SHORT).show();
                // Có thể điều hướng đến chi tiết thông báo nếu cần
            }

            @Override
            public void onNotificationDelete(Notification notification) {
                viewModel.deleteNotification(notification.getId());
                Toast.makeText(requireContext(), R.string.notification_deleted, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        binding.progressBar.setVisibility(View.VISIBLE);
        if (viewModel.getNotifications() == null) {
            binding.recyclerViewNotifications.setVisibility(View.GONE);
            binding.textViewNoNotifications.setVisibility(View.VISIBLE);
            return;
        }
        viewModel.getNotifications().observe(getViewLifecycleOwner(), notifications -> {
            binding.progressBar.setVisibility(View.GONE);
            if (notifications != null && !notifications.isEmpty()) {
                adapter.submitList(notifications);
                binding.recyclerViewNotifications.setVisibility(View.VISIBLE);
                binding.textViewNoNotifications.setVisibility(View.GONE);
            } else {
                binding.recyclerViewNotifications.setVisibility(View.GONE);
                binding.textViewNoNotifications.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}