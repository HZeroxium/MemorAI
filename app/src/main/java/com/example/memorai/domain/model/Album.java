package com.example.memorai.domain.model;

public class Album {
    private final String title;
    private final String thumbnailUrl;

    public Album(String title, String thumbnailUrl) {
        this.title = title;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
