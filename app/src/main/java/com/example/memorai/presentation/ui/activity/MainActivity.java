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

        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check Dark Mode state from SharedPreferences
        boolean isDarkModeEnabled = getSharedPreferences("settings", MODE_PRIVATE)
                .getBoolean("dark_mode", false);

        // Apply the saved theme mode
        AppCompatDelegate.setDefaultNightMode(
                isDarkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Set the initial state of the Dark Mode switch
        binding.switchDarkMode.setChecked(isDarkModeEnabled);

        // Handle switch toggling for Dark Mode
        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> new Thread(() -> {
            // Save the Dark Mode state off the main thread
            getSharedPreferences("settings", MODE_PRIVATE)
                    .edit()
                    .putBoolean("dark_mode", isChecked)
                    .apply();

            // Apply the theme back on the main thread
            runOnUiThread(() -> AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            ));
        }).start());

        // Load the initial fragment if no saved state exists
        if (savedInstanceState == null) {
            replaceFragment(new PhotoListFragment(), false);
        }
    }

    /**
     * Replace the current fragment with the provided one.
     *
     * @param fragment       The new fragment to display.
     * @param addToBackStack Whether to add the transaction to the back stack.
     */
    private void replaceFragment(Fragment fragment, boolean addToBackStack) {
        var transaction = getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment);

        if (addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null; // Prevent memory leaks
    }
}
