// presentation/ui/activity/MainActivity.java
package com.example.memorai.presentation.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.memorai.R;
import com.example.memorai.databinding.ActivityMainBinding;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SharedPreferences sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE);
        boolean darkMode = sharedPreferences.getBoolean("night", false);
        AlbumViewModel albumViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(AlbumViewModel.class);
        albumViewModel.ensureDefaultAlbumExists(); // Ensure default album exists

//        setupDarkMode();
        setupNavigation();
        setupProfileIcon();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(updateBaseContextLocale(newBase));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String language = prefs.getString("Language", "en"); // Mặc định là English
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
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
                if (destination.getId() == R.id.photoDetailFragment || destination.getId() == R.id.addPhotoFragment || destination.getId() == R.id.settingsFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                    binding.header.setVisibility(View.GONE);
                    binding.searchBar.setVisibility(View.GONE);
                } else {
                    binding.bottomNavigation.setVisibility(View.VISIBLE);
                    binding.header.setVisibility(View.VISIBLE);
                    binding.headerText.setText(R.string.hi);
                    MenuItem photosItem = binding.bottomNavigation.getMenu().findItem(R.id.photoListFragment);
                    MenuItem albumsItem = binding.bottomNavigation.getMenu().findItem(R.id.albumListFragment);
                    MenuItem searchItem = binding.bottomNavigation.getMenu().findItem(R.id.searchFragment);

                    if (photosItem != null) photosItem.setTitle(R.string.photos);
                    if (albumsItem != null) albumsItem.setTitle(R.string.albums);
                    if (searchItem != null) searchItem.setTitle(R.string.search);

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
            for (int i = 0; i < popupMenu.getMenu().size(); i++) {
                MenuItem menuItem = popupMenu.getMenu().getItem(i);
                SpannableString s = new SpannableString(menuItem.getTitle());
                s.setSpan(
                        new ForegroundColorSpan(getResources().getColor(R.color.md_theme_onBackground)),
                        0, s.length(), 0
                );
                menuItem.setTitle(s);
            }
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