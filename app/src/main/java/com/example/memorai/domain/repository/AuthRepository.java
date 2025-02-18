// domain/repository/AuthRepository.java
package com.example.memorai.domain.repository;

import com.example.memorai.domain.model.User;

public interface AuthRepository {
    // Sign in with email and password
    User signIn(String email, String password);

    void signOut();

    User getCurrentUser();
}
