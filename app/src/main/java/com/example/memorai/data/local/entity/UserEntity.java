// data/local/entity/UserEntity.java
package com.example.memorai.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class UserEntity {
    @PrimaryKey
    @ColumnInfo(name = "id")
    @NonNull
    public String id = "";

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "profile_picture_url")
    public String profilePictureUrl;
}


