// domain/model/User.java
package com.example.memorai.domain.model;

import java.util.Objects;

public final class User {
    private final String id;
    private final String name;
    private final String email;
    private final String profilePictureUrl;

    public User(String id, String name, String email, String profilePictureUrl) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.profilePictureUrl = profilePictureUrl;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(name, user.name) &&
                Objects.equals(email, user.email) &&
                Objects.equals(profilePictureUrl, user.profilePictureUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, profilePictureUrl);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", profilePictureUrl='" + profilePictureUrl + '\'' +
                '}';
    }
}
