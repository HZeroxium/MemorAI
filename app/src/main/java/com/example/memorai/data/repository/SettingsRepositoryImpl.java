// data/repository/SettingsRepositoryImpl.java
package com.example.memorai.data.repository;

import android.content.SharedPreferences;

import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.domain.repository.SettingsRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SettingsRepositoryImpl implements SettingsRepository {

    private final SharedPreferences sharedPreferences;

    @Inject
    public SettingsRepositoryImpl(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public AppSettings getSettings() {
        boolean darkMode = sharedPreferences.getBoolean("dark_mode", false);
        String language = sharedPreferences.getString("language", "en");
        boolean cloudSyncEnabled = sharedPreferences.getBoolean("cloud_sync", false);
        boolean biometricAuthEnabled = sharedPreferences.getBoolean("biometric_auth", false);
        return new AppSettings(darkMode, language, cloudSyncEnabled, biometricAuthEnabled);
    }

    @Override
    public void updateSettings(AppSettings settings) {
        sharedPreferences.edit()
                .putBoolean("dark_mode", settings.isDarkMode())
                .putString("language", settings.getLanguage())
                .putBoolean("cloud_sync", settings.isCloudSyncEnabled())
                .putBoolean("biometric_auth", settings.isBiometricAuthEnabled())
                .apply();
    }
}
