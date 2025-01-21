// domain/model/Album.java
package com.example.memorai.domain.model;

public class Album {
    private int id;
    private String name;
    private long createdAt;

    public Album(String name) {
        this.name = name;
    }

    public Album(String name, long createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
