// domain/model/AppSettings.java
package com.example.memorai.domain.model;

public final class AppSettings {
    private final boolean darkMode;
    private final String language;
    private final boolean cloudSyncEnabled;
    private final boolean biometricAuthEnabled;

    public AppSettings(boolean darkMode, String language, boolean cloudSyncEnabled, boolean biometricAuthEnabled) {
        this.darkMode = darkMode;
        this.language = language;
        this.cloudSyncEnabled = cloudSyncEnabled;
        this.biometricAuthEnabled = biometricAuthEnabled;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isCloudSyncEnabled() {
        return cloudSyncEnabled;
    }

    public boolean isBiometricAuthEnabled() {
        return biometricAuthEnabled;
    }

    @Override
    public String toString() {
        return "AppSettings{" +
                "darkMode=" + darkMode +
                ", language='" + language + '\'' +
                ", cloudSyncEnabled=" + cloudSyncEnabled +
                ", biometricAuthEnabled=" + biometricAuthEnabled +
                '}';
    }
}
