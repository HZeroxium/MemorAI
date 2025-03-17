// presentation/ui/adapter/PhotoSection.java
package com.example.memorai.presentation.ui.adapter;

import com.example.memorai.domain.model.Photo;

import java.util.List;

public class PhotoSection {
    private final String label; // e.g. "Today", "February 2025"
    private final List<Photo> photos;

    public PhotoSection(String label, List<Photo> photos) {
        this.label = label;
        this.photos = photos;
    }

    public String getLabel() {
        return label;
    }

    public List<Photo> getPhotos() {
        return photos;
    }
}
