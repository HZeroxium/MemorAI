package com.example.memorai.domain.model;

import android.graphics.Bitmap;
import android.util.Base64;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Photo {
    final private String id;
    private String filePath;
    private Bitmap bitmap;
    private boolean isPrivate;
    private List<String> tags; // Removed the final modifier
    final private long createdAt;
    final private long updatedAt;

    public Photo() {
        this.id = "";
        this.filePath = "";
        this.tags = new ArrayList<>(); // Use ArrayList instead of Collections.emptyList()
        this.createdAt = 0;
        this.updatedAt = 0;
        this.isPrivate = false;
    }

    public Photo(String id, String filePath, List<String> tags, long createdAt, long updatedAt, boolean isPrivate) {
        this.id = id;
        this.filePath = filePath;
        // Store a copy of the tags list instead of making it immutable
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isPrivate = isPrivate;
    }

    public Photo(String id, String filePath, List<String> tags, long createdAt, long updatedAt) {
        this(id, filePath, tags, createdAt, updatedAt, false);
    }

    public Photo(String id, String filePath, List<String> tags) {
        this(id, filePath, tags, System.currentTimeMillis(), System.currentTimeMillis());
    }

    public Photo(String id, String filePath) {
        this(id, filePath, null);
    }

    public String getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public List<String> getTags() {
        return tags;
    }

    // Add a new method to set tags
    public void setTags(List<String> tags) {
        this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void seIsPrivate(boolean state) {
        this.isPrivate = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Photo))
            return false;
        Photo photo = (Photo) o;
        return createdAt == photo.createdAt &&
                updatedAt == photo.updatedAt &&
                isPrivate == photo.isPrivate &&
                Objects.equals(id, photo.id) &&
                Objects.equals(filePath, photo.filePath) &&
                Objects.equals(tags, photo.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filePath, tags, createdAt, updatedAt, isPrivate);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id='" + id + '\'' +
                ", filePath='" + filePath + '\'' +
                ", tags=" + tags +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", isPrivate=" + isPrivate +
                '}';
    }
}