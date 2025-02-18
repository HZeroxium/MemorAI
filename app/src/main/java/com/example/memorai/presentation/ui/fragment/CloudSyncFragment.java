// presentation/ui/fragment/CloudSyncFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.memorai.databinding.FragmentCloudSyncBinding;
import com.example.memorai.presentation.viewmodel.CloudSyncViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CloudSyncFragment extends Fragment {

    private FragmentCloudSyncBinding binding;
    private CloudSyncViewModel cloudSyncViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCloudSyncBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        cloudSyncViewModel = new ViewModelProvider(this).get(CloudSyncViewModel.class);

        binding.btnSyncPhotos.setOnClickListener(v -> {
            // Here, you might pass a list of local photos; for demo, we pass an empty list.
            cloudSyncViewModel.syncPhotos(List.of());
        });

        cloudSyncViewModel.getSyncStatus().observe(getViewLifecycleOwner(), status -> {
            binding.textViewSyncStatus.setText(status ? "Sync Completed" : "Sync In Progress");
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
