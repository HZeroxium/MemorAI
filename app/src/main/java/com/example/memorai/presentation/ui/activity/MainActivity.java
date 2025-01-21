// presentation/ui/activity/MainActivity.java
package com.example.memorai.presentation.ui.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.memorai.R;
import com.example.memorai.databinding.ActivityMainBinding;
import com.example.memorai.presentation.ui.fragment.PhotoListFragment;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        boolean isDarkModeEnabled = getSharedPreferences("settings", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
        binding.switchDarkMode.setChecked(isDarkModeEnabled);

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Thread(() -> {
                getSharedPreferences("settings", MODE_PRIVATE)
                        .edit()
                        .putBoolean("dark_mode", isChecked)
                        .apply();
                runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(
                        isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                ));
            }).start();
        });

        if (savedInstanceState == null) replaceFragment(new PhotoListFragment(), false);
    }

    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        var transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment);
        if (addToBackStack) transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}