package com.example.memorai.presentation.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentSettingsBinding;
import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.presentation.viewmodel.SettingsViewModel;

import java.util.Arrays;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SettingsViewModel settingsViewModel;

    boolean darkMode;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        sharedPreferences = requireContext().getSharedPreferences("Mode", Context.MODE_PRIVATE);
        darkMode = sharedPreferences.getBoolean("night", false);
        if (darkMode) {
            binding.switchDarkMode.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            binding.switchDarkMode.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        binding.switchDarkMode.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onClick(View v) {
                darkMode = !darkMode;
                if (darkMode) {
                    // Bật chế độ dark mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", true);
                } else {
                    // Tắt chế độ dark mode
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    editor = sharedPreferences.edit();
                    editor.putBoolean("night", false);
                }
                editor.apply();
            }
        });
        return binding.settingsLayout;
    }



    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(getViewLifecycleOwner(), this::updateUI);
        settingsViewModel.loadSettings();


        // Listener for dark mode switch
//        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
//            AppSettings current = settingsViewModel.getSettings().getValue();
//            if (current != null) {
//                AppSettings updated = new AppSettings(isChecked, current.getLanguage(), current.isCloudSyncEnabled(), current.isBiometricAuthEnabled());
//                settingsViewModel.updateSettings(updated);
//                AppCompatDelegate.setDefaultNightMode(isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
//            }
//        });

        // Listener for language Spinner
        binding.textViewLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedLanguage = (String) parent.getItemAtPosition(position);
                AppSettings current = settingsViewModel.getSettings().getValue();
                if (current != null) {
                    AppSettings updated = new AppSettings(current.isDarkMode(), selectedLanguage, current.isCloudSyncEnabled(), current.isBiometricAuthEnabled());
                    settingsViewModel.updateSettings(updated);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        binding.btnExit.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());


        // Similar listeners for cloud sync and biometric auth can be added here
    }

    private void updateUI(AppSettings settings) {
        // Update dark mode switch

        // Update language Spinner
        List<String> languages = Arrays.asList("English", "Spanish", "French", "German");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), // Use requireContext() for Fragment
                android.R.layout.simple_spinner_item, // Default layout for each item
                languages // List of items
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.textViewLanguage.setAdapter(adapter);

        // Set the selected language in the Spinner
        int languagePosition = adapter.getPosition(settings.getLanguage());
        binding.textViewLanguage.setSelection(languagePosition);

        // Update cloud sync switch
        binding.switchCloudSync.setChecked(settings.isCloudSyncEnabled());

        // Update other UI elements as needed
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}