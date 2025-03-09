// presentation/ui/activity/MainActivity.java
package com.example.memorai.presentation.ui.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.memorai.R;
import com.example.memorai.databinding.ActivityMainBinding;
import com.example.memorai.presentation.ui.dialog.ModalBottomSheetAddMenu;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.utils.notification.MemoryReceiver;

import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private AlbumViewModel albumViewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkMode();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        albumViewModel.ensureDefaultAlbumExists();
        SharedPreferences sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE);
        boolean darkMode = sharedPreferences.getBoolean("night", false);
        AlbumViewModel albumViewModel = new ViewModelProvider((ViewModelStoreOwner) this).get(AlbumViewModel.class);
        albumViewModel.ensureDefaultAlbumExists(); // Ensure default album exists

//        setupDarkMode();
        setupNavigation();
        setupProfileIcon();
        setupNotificationButton();
        requestNotificationPermission();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }
    private void applyDarkMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE);
        boolean darkMode = sharedPreferences.getBoolean("night", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
      
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
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
              
                boolean isHiddenScreen = destination.getId() == R.id.photoListFragment ||
                        destination.getId() == R.id.albumListFragment ||
                        destination.getId() == R.id.searchFragment;

                toggleUIVisibility(isHiddenScreen);
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
>>>>>>> master
            });

        } catch (IllegalStateException e) {
            throw new IllegalStateException("Failed to initialize NavHostFragment: " + e.getMessage());
        }
    }

    private void toggleUIVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        binding.bottomNavigation.setVisibility(visibility);
        binding.toolbar.setVisibility(visibility);
    }

    private void setupProfileIcon() {
        binding.profileIcon.setOnClickListener(this::showProfileMenu);
    }

    private void setupNotificationButton() {
        binding.buttonNotification.setOnClickListener(v -> {navController.navigate(R.id.notificationFragment);});
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());

        for (int i = 0; i < popupMenu.getMenu().size(); i++) {
            setTitleSpan(popupMenu.getMenu().getItem(i));
        }

        popupMenu.setOnMenuItemClickListener(this::handleProfileMenuClick);
        popupMenu.show();
    }

    private void setTitleSpan(MenuItem menuItem) {
        SpannableString s = new SpannableString(menuItem.getTitle());
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.md_theme_onBackground)),
                0, s.length(), 0);
        menuItem.setTitle(s);
    }

    private boolean handleProfileMenuClick(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_profile) {
            navController.navigate(R.id.profileFragment);
        } else if (itemId == R.id.menu_settings) {
            navController.navigate(R.id.settingsFragment);
            toggleUIVisibility(false);
        } else if (itemId == R.id.menu_login) {
            navController.navigate(R.id.loginFragment);
        } else {
            return false;
        }
        return true;
    }
}
