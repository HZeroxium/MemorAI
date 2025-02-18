// domain/usecase/settings/UpdateAppSettingsUseCase.java
package com.example.memorai.domain.usecase.settings;

import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.domain.repository.SettingsRepository;

import javax.inject.Inject;

public class UpdateAppSettingsUseCase {
    private final SettingsRepository settingsRepository;

    @Inject
    public UpdateAppSettingsUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public void execute(AppSettings settings) {
        settingsRepository.updateSettings(settings);
    }
}
