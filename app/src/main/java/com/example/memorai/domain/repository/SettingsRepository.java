// domain/repository/SettingsRepository.java
package com.example.memorai.domain.repository;

import com.example.memorai.domain.model.AppSettings;

public interface SettingsRepository {
    AppSettings getSettings();

    void updateSettings(AppSettings settings);
}

