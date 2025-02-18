// domain/model/Album.java
package com.example.memorai.domain.model;

import java.util.Objects;

public final class Album {
    private final String id;
    private final String name;
    private final String description;
    private final String coverPhotoUrl;
    private final long createdAt;
    private final long updatedAt;

    public Album(String id, String name, String description, String coverPhotoUrl, long createdAt, long updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.coverPhotoUrl = coverPhotoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverPhotoUrl() {
        return coverPhotoUrl;
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
        if (!(o instanceof Album)) return false;
        Album album = (Album) o;
        return createdAt == album.createdAt &&
                updatedAt == album.updatedAt &&
                Objects.equals(id, album.id) &&
                Objects.equals(name, album.name) &&
                Objects.equals(description, album.description) &&
                Objects.equals(coverPhotoUrl, album.coverPhotoUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, coverPhotoUrl, createdAt, updatedAt);
    }

    @Override
    public String toString() {
        return "Album{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", coverPhotoUrl='" + coverPhotoUrl + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
