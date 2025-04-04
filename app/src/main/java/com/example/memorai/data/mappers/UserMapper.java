// data/mappers/UserMapper.java
package com.example.memorai.data.mappers;

import com.example.memorai.data.local.entity.UserEntity;
import com.example.memorai.domain.model.User;

public class UserMapper {
    public static User toDomain(UserEntity entity) {
        return new User(entity.id, entity.name, entity.email, entity.profilePictureUrl, entity.pin);
    }

    public static UserEntity fromDomain(User user) {
        UserEntity entity = new UserEntity();
        entity.id = user.getId();
        entity.name = user.getName();
        entity.email = user.getEmail();
        entity.profilePictureUrl = user.getProfilePictureUrl();
        return entity;
    }
}
