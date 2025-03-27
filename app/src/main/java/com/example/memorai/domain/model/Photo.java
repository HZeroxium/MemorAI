package com.example.memorai.domain.model;

import android.graphics.Bitmap;
import android.util.Base64;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Photo {
    final private String id;
    private String filePath;
    private Bitmap bitmap;
    final private List<String> tags;
    final private long createdAt;
    final private long updatedAt;

    public Photo() {
        this.id = "";
        this.filePath = "";
        this.tags = Collections.emptyList();
        this.createdAt = 0;
        this.updatedAt = 0;
    }

    public Photo(String id, String filePath, List<String> tags, long createdAt, long updatedAt) {
        this.id = id;
        this.filePath = filePath;
        // Ensure immutability of the tags list
        this.tags = tags != null ? Collections.unmodifiableList(tags) : Collections.emptyList();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof Photo)) return false;
        Photo photo = (Photo) o;
        return createdAt == photo.createdAt &&
                updatedAt == photo.updatedAt &&
                Objects.equals(id, photo.id) &&
                Objects.equals(filePath, photo.filePath) &&
                Objects.equals(tags, photo.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filePath, tags, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id='" + id + '\'' +
                ", filePath='" + filePath + '\'' +
                ", tags=" + tags +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
