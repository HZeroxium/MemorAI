package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.domain.usecase.settings.GetAppSettingsUseCase;
import com.example.memorai.domain.usecase.settings.UpdateAppSettingsUseCase;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SettingsViewModel extends ViewModel {
    private final GetAppSettingsUseCase getAppSettingsUseCase;
    private final UpdateAppSettingsUseCase updateAppSettingsUseCase;

    private final MutableLiveData<AppSettings> settingsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> statusMessage = new MutableLiveData<>();

    @Inject
    public SettingsViewModel(GetAppSettingsUseCase getAppSettingsUseCase,
                             UpdateAppSettingsUseCase updateAppSettingsUseCase) {
        this.getAppSettingsUseCase = getAppSettingsUseCase;
        this.updateAppSettingsUseCase = updateAppSettingsUseCase;
    }

    public LiveData<AppSettings> getSettings() {
        return settingsLiveData;
    }

    public LiveData<String> getStatusMessage() {
        return statusMessage;
    }

    public void loadSettings() {
        new Thread(() -> {
            try {
                AppSettings settings = getAppSettingsUseCase.execute();
                settingsLiveData.postValue(settings);
            } catch (Exception e) {
                statusMessage.postValue("Failed to load settings: " + e.getMessage());
            }
        }).start();
    }

    public void updateSettings(AppSettings settings) {
        new Thread(() -> {
            try {
                updateAppSettingsUseCase.execute(settings);
                settingsLiveData.postValue(settings);
                statusMessage.postValue("Settings updated successfully");
            } catch (Exception e) {
                statusMessage.postValue("Failed to update settings: " + e.getMessage());
            }
        }).start();
    }

    public void setCloudSyncEnabled(boolean isEnabled) {
        new Thread(() -> {
            try {
                AppSettings currentSettings = getAppSettingsUseCase.execute();
                if (currentSettings.isCloudSyncEnabled() != isEnabled) {
                    AppSettings updatedSettings = currentSettings.withCloudSyncEnabled(isEnabled);
                    updateAppSettingsUseCase.execute(updatedSettings);
                    settingsLiveData.postValue(updatedSettings);
                    statusMessage.postValue("Cloud sync " + (isEnabled ? "enabled" : "disabled"));
                }
            } catch (Exception e) {
                statusMessage.postValue("Failed to update cloud sync: " + e.getMessage());
            }
        }).start();
    }

    public void setLanguage(String languageCode) {
        new Thread(() -> {
            try {
                AppSettings currentSettings = getAppSettingsUseCase.execute();
                if (!currentSettings.getLanguage().equals(languageCode)) {
                    AppSettings updatedSettings = currentSettings.withLanguage(languageCode);
                    updateAppSettingsUseCase.execute(updatedSettings);
                    settingsLiveData.postValue(updatedSettings);
                    statusMessage.postValue("Language updated to " + languageCode);
                }
            } catch (Exception e) {
                statusMessage.postValue("Failed to update language: " + e.getMessage());
            }
        }).start();
    }

    public void setDarkModeEnabled(boolean isEnabled) {
        new Thread(() -> {
            try {
                AppSettings currentSettings = getAppSettingsUseCase.execute();
                if (currentSettings.isDarkModeEnabled() != isEnabled) {
                    AppSettings updatedSettings = currentSettings.withDarkModeEnabled(isEnabled);
                    updateAppSettingsUseCase.execute(updatedSettings);
                    settingsLiveData.postValue(updatedSettings);
                    statusMessage.postValue("Dark mode " + (isEnabled ? "enabled" : "disabled"));
                }
            } catch (Exception e) {
                statusMessage.postValue("Failed to update dark mode: " + e.getMessage());
            }
        }).start();
    }
}