// domain/model/Album.java
package com.example.memorai.domain.model;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Album {
    private final String id;
    private final String name;
    private final String description;
    private String coverPhotoUrl;
    private Bitmap bitmap;
    private boolean isPrivate = false;
    private final long createdAt;
    private final List<String> photos;

    private final long updatedAt;

    public Album() {
        this.id = "";
        this.name = "";
        this.description = "";
        this.photos = new ArrayList<>();
        this.coverPhotoUrl = null;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.isPrivate = false;
    }

    public Album(String id, String name, String description, List<String> photos, String coverPhotoUrl, long createdAt, long updatedAt, boolean isPrivate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.photos = photos;
        this.coverPhotoUrl = coverPhotoUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isPrivate = isPrivate;
    }


    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<String> getPhotos() { return photos; }

    public String getDescription() {
        return description;
    }

    public String getCoverPhotoUrl() {
        return coverPhotoUrl;
    }


    public  void setCoverPhotoUrl(String coverPhotoUrl) {
        this.coverPhotoUrl = coverPhotoUrl;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isPrivate() {
        return isPrivate;
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

    @NonNull
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
