// presentation/ui/fragment/SettingsFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.memorai.databinding.FragmentSettingsBinding;
import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.presentation.viewmodel.SettingsViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SettingsViewModel settingsViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(getViewLifecycleOwner(), this::updateUI);
        settingsViewModel.loadSettings();

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppSettings current = settingsViewModel.getSettings().getValue();
            if (current != null) {
                AppSettings updated = new AppSettings(isChecked, current.getLanguage(), current.isCloudSyncEnabled(), current.isBiometricAuthEnabled());
                settingsViewModel.updateSettings(updated);
                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Similar listeners for language and cloud sync can be added here
    }

    private void updateUI(AppSettings settings) {
        binding.switchDarkMode.setChecked(settings.isDarkMode());
        binding.textViewLanguage.setText(settings.getLanguage());
        binding.switchCloudSync.setChecked(settings.isCloudSyncEnabled());
        // etc.
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
