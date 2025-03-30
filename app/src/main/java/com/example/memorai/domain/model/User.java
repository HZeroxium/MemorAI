package com.example.memorai.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

public final class User implements Parcelable {
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

    // Constructor để khôi phục dữ liệu từ Parcel
    protected User(Parcel in) {
        id = in.readString();
        name = in.readString();
        email = in.readString();
        profilePictureUrl = in.readString();
    }

    // Ghi dữ liệu vào Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(profilePictureUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // CREATOR để tạo User từ Parcel
    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // Getter methods
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
