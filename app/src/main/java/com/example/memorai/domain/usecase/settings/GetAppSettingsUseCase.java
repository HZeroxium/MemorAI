// domain/usecase/settings/GetAppSettingsUseCase.java
package com.example.memorai.domain.usecase.settings;

import com.example.memorai.domain.model.AppSettings;
import com.example.memorai.domain.repository.SettingsRepository;

import javax.inject.Inject;

public class GetAppSettingsUseCase {
    private final SettingsRepository settingsRepository;

    @Inject
    public GetAppSettingsUseCase(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public AppSettings execute() {
        return settingsRepository.getSettings();
    }
}
