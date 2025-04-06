package com.example.memorai.presentation.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentSettingsBinding;
import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.example.memorai.presentation.viewmodel.SettingsViewModel;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private AlbumViewModel albumViewModel;

    private PhotoViewModel photoViewModel;
    private SettingsViewModel settingsViewModel;

    boolean darkMode;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        sharedPreferences = requireContext().getSharedPreferences("Mode", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);


        darkMode = sharedPreferences.getBoolean("night", false);
        if (darkMode) {
            binding.switchDarkMode.setChecked(true);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            binding.switchDarkMode.setChecked(false);
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Listener cho switchDarkMode
        binding.switchDarkMode.setOnClickListener(v -> {
            darkMode = !darkMode;
            if (darkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("night", true);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("night", false);
            }
            editor.apply();
        });

        binding.switchCloudSync.setOnClickListener(v -> {
            boolean isChecked = binding.switchCloudSync.isChecked();
            SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor settingsEditor = prefs.edit();
            settingsEditor.putBoolean("CloudSync", isChecked);
            settingsEditor.apply();

            if (isChecked) {
                photoViewModel.syncPhoto();
//                albumViewModel.syncAllPendingChanges();
            }
        });

        return binding.getRoot();
    }

    private void setLocale(String lang) {
        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("Language", lang);
        editor.apply();

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());

        getParentFragmentManager().beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        settingsViewModel.getSettings().observe(getViewLifecycleOwner(), this::updateUI);
        settingsViewModel.loadSettings();

        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String savedLanguage = prefs.getString("Language", "en");

        // Cài đặt Spinner ngôn ngữ
        String[] languages = {"Tiếng Việt", "China", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, languages);
        binding.spinnerLanguage.setAdapter(adapter);

        int position = 2; // Mặc định là English
        if (savedLanguage.equals("vi")) {
            position = 0;
        } else if (savedLanguage.equals("zh")) {
            position = 1;
        }
        binding.spinnerLanguage.setSelection(position);

        binding.spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        setLocale("vi");
                        break;
                    case 1:
                        setLocale("zh");
                        break;
                    case 2:
                        setLocale("en");
                        break;
                }
                updateUIText();
            }

            private void updateUIText() {
                binding.tvSettings.setText(getString(R.string.settings));
                binding.tvDarkMode.setText(getString(R.string.dark_mode));
                binding.tvLanguage.setText(getString(R.string.language));
                binding.tvSync.setText(getString(R.string.synchronize));
                binding.btnChangePin.setText(getString(R.string.change_pin));
                binding.btnResetSystem.setText(getString(R.string.reset_system));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void updateUI(AppSettings settings) {
        binding.switchCloudSync.setChecked(settings.isCloudSyncEnabled());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}