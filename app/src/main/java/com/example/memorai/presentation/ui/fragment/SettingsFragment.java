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
import com.example.memorai.presentation.viewmodel.SettingsViewModel;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;

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

        // Load lại fragment thay vì recreate() toàn bộ Activity
        getParentFragmentManager().beginTransaction()
                .detach(this)
                .attach(this)
                .commit();
    }




    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        SettingsViewModel settingsViewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        settingsViewModel.getSettings().observe(getViewLifecycleOwner(), this::updateUI);
        settingsViewModel.loadSettings();

        SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String savedLanguage = prefs.getString("Language", "en"); // Mặc định là English


        // Listener for language Spinner

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
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                switch (position) {
                    case 0:
                        setLocale("vi"); // Tiếng Việt
                        break;
                    case 1:
                        setLocale("zh"); // Tiếng Trung
                        break;
                    case 2:
                        setLocale("en"); // Tiếng Anh
                        break;
                }
                updateUIText(); // Cập nhật lại giao diện người dùng
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



        // Similar listeners for cloud sync and biometric auth can be added here
    }

    private void updateUI(AppSettings settings) {
        // Update dark mode switch



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