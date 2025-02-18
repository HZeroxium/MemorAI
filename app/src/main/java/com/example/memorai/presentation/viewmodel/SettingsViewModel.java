// presentation/viewmodel/SettingsViewModel.java
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

    @Inject
    public SettingsViewModel(GetAppSettingsUseCase getAppSettingsUseCase,
                             UpdateAppSettingsUseCase updateAppSettingsUseCase) {
        this.getAppSettingsUseCase = getAppSettingsUseCase;
        this.updateAppSettingsUseCase = updateAppSettingsUseCase;
    }

    public LiveData<AppSettings> getSettings() {
        return settingsLiveData;
    }

    public void loadSettings() {
        new Thread(() -> {
            AppSettings settings = getAppSettingsUseCase.execute();
            settingsLiveData.postValue(settings);
        }).start();
    }

    public void updateSettings(AppSettings settings) {
        new Thread(() -> {
            updateAppSettingsUseCase.execute(settings);
            settingsLiveData.postValue(settings);
        }).start();
    }
}
