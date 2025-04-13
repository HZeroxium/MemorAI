package com.example.memorai.presentation.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentSettingsBinding;
import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.presentation.ui.activity.MainActivity;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.example.memorai.presentation.viewmodel.SettingsViewModel;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.example.memorai.utils.notification.NotificationHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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

        binding.btnChangePin.setOnClickListener(v -> {
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser == null) {
                Toast.makeText(requireContext(), "Người dùng chưa đăng nhập", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = firebaseUser.getUid();

            showPinFragmentForVerification(userId);
        });

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
            prefs.edit().putBoolean("CloudSync", isChecked).apply();

            if (isChecked) {
                // Tạo dialog loading
                AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                        .setView(R.layout.dialog_loading) // Tạo file layout dialog_loading.xml với ProgressBar
                        .setCancelable(false)
                        .create();
                loadingDialog.show();

                // Vô hiệu hóa switch trong khi đồng bộ
                binding.switchCloudSync.setEnabled(false);

                // Đồng bộ cả ảnh và album
                photoViewModel.syncPhoto(new PhotoViewModel.SyncCallback() {
                    @Override
                    public void onSyncStarted() {
                        // Không cần xử lý gì đặc biệt
                    }

                    @Override
                    public void onSyncCompleted(boolean isPhotoSyncSuccess) {
                        // Sau khi đồng bộ ảnh xong, tiếp tục đồng bộ album
                        albumViewModel.syncAllPendingChanges(new AlbumViewModel.SyncCallback() {
                            @Override
                            public void onSyncStarted() {
                                // Có thể cập nhật message dialog nếu muốn
                            }

                            @Override
                            public void onSyncCompleted(boolean isAlbumSyncSuccess) {
                                // Ẩn dialog loading khi hoàn thành cả hai
                                loadingDialog.dismiss();
                                binding.switchCloudSync.setEnabled(true);

                                if (!isPhotoSyncSuccess || !isAlbumSyncSuccess) {
                                    binding.switchCloudSync.setChecked(false);
                                    Toast.makeText(requireContext(),
                                            getString(R.string.sync_failed,
                                                    (!isPhotoSyncSuccess ? getString(R.string.photos) : "") +
                                                            (!isPhotoSyncSuccess && !isAlbumSyncSuccess
                                                                    ? getString(R.string.and)
                                                                    : "")
                                                            +
                                                            (!isAlbumSyncSuccess ? getString(R.string.albums) : "")),
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    photoViewModel.loadAllPhotos();
                                    albumViewModel.loadAlbums();
                                    if (getContext() != null) {
                                        NotificationHelper.createNotificationChannel(getContext(), "sync_channel");
                                        Intent intent = new Intent(requireContext(), MainActivity.class);
                                        intent.setFlags(
                                                Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        intent.putExtra("REFRESH_UI", true); // Thêm extra để yêu cầu làm mới UI
                                        NotificationHelper.sendSystemNotification(
                                                requireContext(),
                                                "sync_channel",
                                                getString(R.string.sync_notification_title),
                                                getString(R.string.sync_notification_message),
                                                intent);
                                    }
                                }
                            }
                        });
                    }
                });
            }
        });

        binding.btnResetSystem.setOnClickListener(v -> {
            // Đặt lại dark mode về mặc định (tắt)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor.putBoolean("night", false);
            editor.apply();
            binding.switchDarkMode.setChecked(false);

            // Đặt lại ngôn ngữ về mặc định (English)
            setLocale("en");
            binding.spinnerLanguage.setSelection(2); // English ở vị trí 2

            // Xóa cài đặt CloudSync nếu muốn reset hoàn toàn (tuỳ chọn)
            SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = prefs.edit();
            prefsEditor.putBoolean("CloudSync", false);
            prefsEditor.putString("Language", "en"); // Cập nhật ngôn ngữ mặc định
            prefsEditor.apply();

            Toast.makeText(requireContext(), getString(R.string.system_reset_success), Toast.LENGTH_SHORT).show();
        });

        // Add click listener for the Exit & Save button
        binding.btnSave.setOnClickListener(v -> {
            // Save all current settings
            editor.apply(); // Ensure dark mode settings are applied

            // Get and save other settings
            SharedPreferences prefs = requireContext().getSharedPreferences("Settings", Context.MODE_PRIVATE);
            String language = prefs.getString("Language", "en");
            boolean cloudSyncEnabled = prefs.getBoolean("CloudSync", false);

            // Update settings in ViewModel for persistence
            if (settingsViewModel != null) {
                settingsViewModel.setDarkModeEnabled(darkMode);
                settingsViewModel.setLanguage(language);
                settingsViewModel.setCloudSyncEnabled(cloudSyncEnabled);

                // Show success message to user
                Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show();
            }

            // Navigate back to previous screen
            NavController navController = NavHostFragment.findNavController(this);
            navController.navigateUp();
        });

        return binding.getRoot();
    }

    private void showPinFragmentForVerification(String userId) {
        PinFragment pinFragment = new PinFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("mode", "verify");
        pinFragment.setArguments(args);

        // Thiết lập listener khi xác thực thành công
        pinFragment.setOnPinVerifiedListener(() -> {
            // Sau khi xác thực thành công, hiển thị PinFragment ở chế độ create
            showPinFragmentForCreation(userId);
        });

        pinFragment.show(getParentFragmentManager(), "PinFragment");
    }

    private void showPinFragmentForCreation(String userId) {
        PinFragment pinFragment = new PinFragment();
        Bundle args = new Bundle();
        args.putString("userId", userId);
        args.putString("mode", "create");
        pinFragment.setArguments(args);
        pinFragment.show(getParentFragmentManager(), "PinFragment");
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
        String[] languages = { "Tiếng Việt", "中文", "English" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, languages);
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
                binding.btnSave.setText(getString(R.string.exit_save));
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