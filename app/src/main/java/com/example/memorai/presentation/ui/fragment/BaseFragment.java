package com.example.memorai.presentation.ui.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import androidx.fragment.app.Fragment;
import java.util.Locale;

public class BaseFragment extends Fragment {
    @Override
    public void onAttach(Context context) {
        super.onAttach(updateBaseContextLocale(context));
    }

    private Context updateBaseContextLocale(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        String language = prefs.getString("Language", "en"); // Mặc định là English
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }
}
