package com.example.memorai.data.repository;

import android.content.SharedPreferences;

import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.domain.repository.SettingsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingsRepositoryImpl implements SettingsRepository {

    private static final String KEY_DARK_MODE = "dark_mode_enabled";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_CLOUD_SYNC = "cloud_sync_enabled";
    private static final String KEY_BIOMETRIC_AUTH = "biometric_auth_enabled";

    private final SharedPreferences sharedPreferences;

    @Inject
    public SettingsRepositoryImpl(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public AppSettings getSettings() {
        boolean darkMode = sharedPreferences.getBoolean(KEY_DARK_MODE, false);
        String language = sharedPreferences.getString(KEY_LANGUAGE, "en");
        boolean cloudSyncEnabled = sharedPreferences.getBoolean(KEY_CLOUD_SYNC, false);
        boolean biometricAuthEnabled = sharedPreferences.getBoolean(KEY_BIOMETRIC_AUTH, false);

        return new AppSettings.Builder()
                .setDarkMode(darkMode)
                .setLanguage(language != null ? language : "en") // Đảm bảo language không null
                .setCloudSyncEnabled(cloudSyncEnabled)
                .setBiometricAuthEnabled(biometricAuthEnabled)
                .build();
    }

    @Override
    public void updateSettings(AppSettings settings) {
        if (settings == null) {
            throw new IllegalArgumentException("Settings cannot be null");
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_DARK_MODE, settings.isDarkModeEnabled());
        editor.putString(KEY_LANGUAGE, settings.getLanguage());
        editor.putBoolean(KEY_CLOUD_SYNC, settings.isCloudSyncEnabled());
        editor.putBoolean(KEY_BIOMETRIC_AUTH, settings.isBiometricAuthEnabled());
        editor.apply();
    }
}