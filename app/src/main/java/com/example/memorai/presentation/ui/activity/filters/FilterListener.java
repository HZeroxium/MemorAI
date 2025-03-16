package com.example.memorai.presentation.ui.activity.filters;

import ja.burhanrashid52.photoeditor.PhotoFilter;
import androidx.annotation.Nullable;
public interface FilterListener {
    void onFilterSelected(@Nullable PhotoFilter photoFilter);
}
