package com.example.memorai.presentation.ui.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
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
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import android.Manifest;
import com.example.memorai.R;
import com.example.memorai.databinding.ActivityMainBinding;
import com.example.memorai.presentation.ui.dialog.ModalBottomSheetAddMenu;
import com.example.memorai.presentation.ui.fragment.LoginFragment;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.presentation.viewmodel.UserViewModel;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.GoogleAuthProvider;


import java.util.Locale;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;

    private UserViewModel userViewModel;

    private NavController navController;

    private PhotoViewModel photoViewModel;

    AlbumViewModel albumViewModel;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        sharedPreferences = getSharedPreferences("Mode", Context.MODE_PRIVATE);
        Boolean darkMode = sharedPreferences.getBoolean("night", false);
        if (darkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        photoViewModel.loadAllPhotos();
        albumViewModel.loadAlbums();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("619405178592-3ri6lcne9ejli7bt6dt6elj3vo0132t0.apps.googleusercontent.com") // Lấy ID Token để xác thực Firebase
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);


        setupObservers();
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
    private void setupObservers() {
        if(userViewModel.getUserLiveData().getValue() == null) {
            updateUI(null);
        }
        userViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                updateUI(user);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI(userViewModel.getUserLiveData().getValue());
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
            navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                boolean isHiddenScreen = destination.getId() == R.id.photoListFragment ||
                        destination.getId() == R.id.albumListFragment ||
                        destination.getId() == R.id.searchFragment;

                toggleUIVisibility(isHiddenScreen);

                MenuItem photosItem = binding.bottomNavigation.getMenu().findItem(R.id.photoListFragment);
                MenuItem albumsItem = binding.bottomNavigation.getMenu().findItem(R.id.albumListFragment);
                MenuItem searchItem = binding.bottomNavigation.getMenu().findItem(R.id.searchFragment);

                if (photosItem != null) photosItem.setTitle(R.string.photos);
                if (albumsItem != null) albumsItem.setTitle(R.string.albums);
                if (searchItem != null) searchItem.setTitle(R.string.search);

                userViewModel.getUserLiveData().observe(this, user -> {
                    updateUI(user);
                });

            });
        } catch (IllegalStateException e) {
            throw new IllegalStateException("Failed to initialize NavHostFragment: " + e.getMessage());
        }
    }

    public PhotoViewModel getPhotoViewModel() {
        return photoViewModel;
    }

    private void toggleUIVisibility(boolean isVisible) {
        int visibility = isVisible ? View.VISIBLE : View.GONE;
        binding.bottomNavigation.setVisibility(visibility);
        binding.header.setVisibility(visibility);
    }


    private void setupProfileIcon() {
        binding.profileIcon.setOnClickListener(this::showProfileMenu);
    }

    private void showProfileMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null);

        MenuItem loginMenuItem = popupMenu.getMenu().findItem(R.id.menu_login);
        if (loginMenuItem != null) {
            loginMenuItem.setTitle(isLoggedIn ? R.string.logout : R.string.login);
        }

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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        int itemId = item.getItemId();
        if (itemId == R.id.menu_profile) {
            navController.navigate(R.id.profileFragment);
        } else if (itemId == R.id.menu_settings) {
            navController.navigate(R.id.settingsFragment);
            toggleUIVisibility(false);
        } else if (itemId == R.id.menu_login) {
            if (user != null) {
                logout();
            } else {
                LoginFragment loginFragment = new LoginFragment();
                loginFragment.show(getSupportFragmentManager(), "LoginFragment");
            }
            return true;
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


    private void logout() {
        FirebaseAuth.getInstance().signOut();
        GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()).signOut()
                .addOnCompleteListener(this, task -> {
                    binding.headerText.setText(getString(R.string.hi, getString(R.string.you)));
                    binding.profileIcon.setImageResource(R.drawable.ic_profile);
                    userViewModel.signOut();
                    updateUI(null);
                    setupProfileIcon();
                });
    }


    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String userName = user.getDisplayName();
            if (userName != null && !userName.trim().isEmpty()) {
                String[] nameParts = userName.trim().split("\\s+");
                userName = nameParts[nameParts.length - 1];
            } else {
                userName = getString(R.string.you); // Sử dụng chuỗi bản địa hóa
            }
            String profilePic = (user.getPhotoUrl() != null) ? user.getPhotoUrl().toString() : "";
            binding.headerText.setText(getString(R.string.hi, userName));
            Glide.with(this)
                    .load(profilePic)
                    .circleCrop()
                    .error(R.drawable.ic_profile)
                    .into(binding.profileIcon);
        } else {
            binding.headerText.setText(getString(R.string.hi, getString(R.string.you))); // Sử dụng chuỗi bản địa hóa
            Glide.with(this)
                    .load(R.drawable.ic_profile)
                    .circleCrop()
                    .error(R.drawable.ic_profile)
                    .into(binding.profileIcon);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
