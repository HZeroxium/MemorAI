// domain/model/Photo.java
package com.example.memorai.domain.model;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Photo {
    private final String id;
    private final String albumId;
    private final String filePath;
    private final List<String> tags; // e.g., "portrait", "landscape", etc.
    private final long createdAt;
    private final long updatedAt;

    public Photo(String id, String albumId, String filePath, List<String> tags, long createdAt, long updatedAt) {
        this.id = id;
        this.albumId = albumId;
        this.filePath = filePath;
        // Ensure immutability of the tags list
        this.tags = tags != null ? Collections.unmodifiableList(tags) : Collections.emptyList();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getAlbumId() {
        return albumId;
    }

    public String getFilePath() {
        return filePath;
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
                Objects.equals(albumId, photo.albumId) &&
                Objects.equals(filePath, photo.filePath) &&
                Objects.equals(tags, photo.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, albumId, filePath, tags, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "id='" + id + '\'' +
                ", albumId='" + albumId + '\'' +
                ", filePath='" + filePath + '\'' +
                ", tags=" + tags +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
