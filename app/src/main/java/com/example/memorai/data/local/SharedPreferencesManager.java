// data/local/SharedPreferencesManager.java
package com.example.memorai.data.local;

import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SharedPreferencesManager {
    private static final String USERNAME_KEY = "username";
    private final SharedPreferences sharedPreferences;

    @Inject
    public SharedPreferencesManager(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    // Lưu username
    public void saveUsername(String username) {
        sharedPreferences.edit().putString(USERNAME_KEY, username).apply();
    }

    // Lấy username
    public String getUsername() {
        return sharedPreferences.getString(USERNAME_KEY, "");
    }

    // Xóa dữ liệu
    public void clearData() {
        sharedPreferences.edit().clear().apply();
    }
}
