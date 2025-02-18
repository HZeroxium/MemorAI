// domain/repository/UserRepository.java
package com.example.memorai.domain.repository;

import com.example.memorai.domain.model.User;

public interface UserRepository {
    User getUserById(String userId);

    void updateUser(User user);
}

