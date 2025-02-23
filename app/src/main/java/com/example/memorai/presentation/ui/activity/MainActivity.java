package com.example.memorai.presentation.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import androidx.lifecycle.ViewModelProvider;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.memorai.R;
import com.example.memorai.databinding.ActivityMainBinding;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AlbumViewModel albumViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        albumViewModel.ensureDefaultAlbumExists(); // Ensure default album exists

        setupNavigation();
        setupProfileIcon();
    }

    private void setupNavigation() {
        try {
            NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.nav_host_fragment);
            if (navHostFragment == null) {
                throw new IllegalStateException("NavHostFragment not found in MainActivity.");
            }
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (destination.getId() == R.id.photoDetailFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                    binding.header.setVisibility(View.GONE);
                    binding.searchBar.setVisibility(View.GONE);
                } else {
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                    binding.header.setVisibility(View.VISIBLE);
                    binding.searchBar.setVisibility(View.VISIBLE);
                }
            });
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Failed to initialize NavHostFragment: " + e.getMessage());
        }
    }

    private void setupProfileIcon() {
        binding.profileIcon.setOnClickListener(v -> {
            // Create a PopupMenu
            PopupMenu popupMenu = new PopupMenu(this, v);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

            // Set click listener for menu items
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menu_profile) {
                        // Navigate to Profile
                        NavController navController = ((NavHostFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment)).getNavController();
                        navController.navigate(R.id.profileFragment);
                        return true;
                    } else if (itemId == R.id.menu_settings) {
                        // Navigate to Settings
                        NavController navController = ((NavHostFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment)).getNavController();
                        navController.navigate(R.id.settingsFragment);
                        binding.header.setVisibility(View.GONE);
                        binding.searchBar.setVisibility(View.GONE);
                        binding.bottomNavigation.setVisibility(View.GONE);
                        return true;
                    } else if (itemId == R.id.menu_login) {
                        // Navigate to Login
                        NavController navController = ((NavHostFragment) getSupportFragmentManager()
                                .findFragmentById(R.id.nav_host_fragment)).getNavController();
                        navController.navigate(R.id.loginFragment);
                        return true;
                    }
                    return false;
                }
            });

            // Show the PopupMenu
            popupMenu.show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}