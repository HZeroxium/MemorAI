// presentation/ui/activity/MainActivity.java
package com.example.memorai.presentation.ui.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.memorai.R;
import com.example.memorai.databinding.ActivityMainBinding;
import com.example.memorai.presentation.ui.dialog.ModalBottomSheetAddMenu;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyDarkMode();
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        AlbumViewModel albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        albumViewModel.ensureDefaultAlbumExists();

        setupNavigation();
        setupProfileIcon();
        setupAddButton();
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


    /**
     * Apply Dark Mode based on SharedPreferences settings.
     */
    private void applyDarkMode() {
        SharedPreferences sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE);
        boolean darkMode = sharedPreferences.getBoolean("night", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );
    }

    /**
     * Setup Navigation Controller and Destination Change Listener.
     */
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
            });

        } catch (IllegalStateException e) {
            throw new IllegalStateException("Failed to initialize NavHostFragment: " + e.getMessage());
        }
    }

    /**
     * Show/Hide UI elements based on navigation destination.
     */
    private void toggleUIVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        binding.bottomNavigation.setVisibility(visibility);
        binding.toolbar.setVisibility(visibility);
    }

    /**
     * Setup Profile Icon to show PopupMenu for navigation.
     */
    private void setupProfileIcon() {
        binding.profileIcon.setOnClickListener(this::showProfileMenu);
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

    /**
     * Apply color span to menu items.
     */
    private void setTitleSpan(MenuItem menuItem) {
        SpannableString s = new SpannableString(menuItem.getTitle());
        s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.md_theme_onBackground)),
                0, s.length(), 0);
        menuItem.setTitle(s);
    }

    /**
     * Handle Profile Menu Item Clicks.
     */
    private boolean handleProfileMenuClick(MenuItem item) {
        if (navController == null) return false;

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


    /**
     * Show BottomSheet when clicking Add Button.
     */
    private void setupAddButton() {
        binding.buttonAdd.setOnClickListener(v -> showAddMenuBottomSheet());
    }

    private void setupNotificationButton() {
        binding.buttonNotification.setOnClickListener(v -> navController.navigate(R.id.notificationFragment));
    }

    private void showAddMenuBottomSheet() {
        ModalBottomSheetAddMenu bottomSheet = new ModalBottomSheetAddMenu(new ModalBottomSheetAddMenu.BottomSheetListener() {
            @Override
            public void onAddAlbum() {
                navController.navigate(R.id.albumCreateFragment);
            }

            @Override
            public void onImportPhoto() {
                navController.navigate(R.id.importPhotoFragment);
            }

            @Override
            public void onTakePhoto() {
                navController.navigate(R.id.takePhotoFragment);
            }
        });

        bottomSheet.show(getSupportFragmentManager(), "ModalBottomSheetAddMenu");
    }
}
