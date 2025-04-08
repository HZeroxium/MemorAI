package com.example.memorai.domain.model;

public final class AppSettings {
    private final boolean darkMode;
    private final String language;
    private final boolean cloudSyncEnabled;
    private final boolean biometricAuthEnabled;

    // Constructor riêng tư cho Builder
    private AppSettings(Builder builder) {
        this.darkMode = builder.darkMode;
        this.language = builder.language;
        this.cloudSyncEnabled = builder.cloudSyncEnabled;
        this.biometricAuthEnabled = builder.biometricAuthEnabled;
    }

    // Getters
    public boolean isDarkModeEnabled() { // Đổi tên để đồng bộ với SettingsViewModel
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

    // Tạo bản sao mới với giá trị thay đổi
    public AppSettings withDarkModeEnabled(boolean darkMode) {
        return new Builder(this).setDarkMode(darkMode).build();
    }

    public AppSettings withLanguage(String language) {
        return new Builder(this).setLanguage(language).build();
    }

    public AppSettings withCloudSyncEnabled(boolean cloudSyncEnabled) {
        return new Builder(this).setCloudSyncEnabled(cloudSyncEnabled).build();
    }

    public AppSettings withBiometricAuthEnabled(boolean biometricAuthEnabled) {
        return new Builder(this).setBiometricAuthEnabled(biometricAuthEnabled).build();
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

    // Builder class
    public static class Builder {
        private boolean darkMode;
        private String language;
        private boolean cloudSyncEnabled;
        private boolean biometricAuthEnabled;

        // Constructor mặc định
        public Builder() {
            this.darkMode = false;
            this.language = "en"; // Ngôn ngữ mặc định là tiếng Anh
            this.cloudSyncEnabled = false;
            this.biometricAuthEnabled = false;
        }

        // Constructor sao chép từ AppSettings hiện có
        public Builder(AppSettings settings) {
            this.darkMode = settings.darkMode;
            this.language = settings.language;
            this.cloudSyncEnabled = settings.cloudSyncEnabled;
            this.biometricAuthEnabled = settings.biometricAuthEnabled;
        }

        public Builder setDarkMode(boolean darkMode) {
            this.darkMode = darkMode;
            return this;
        }

        public Builder setLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder setCloudSyncEnabled(boolean cloudSyncEnabled) {
            this.cloudSyncEnabled = cloudSyncEnabled;
            return this;
        }

        public Builder setBiometricAuthEnabled(boolean biometricAuthEnabled) {
            this.biometricAuthEnabled = biometricAuthEnabled;
            return this;
        }

        public AppSettings build() {
            return new AppSettings(this);
        }
    }
}