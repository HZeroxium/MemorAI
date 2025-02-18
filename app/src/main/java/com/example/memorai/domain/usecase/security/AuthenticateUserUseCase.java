// domain/usecase/security/AuthenticateUserUseCase.java
package com.example.memorai.domain.usecase.security;

import com.example.memorai.domain.model.User;
import com.example.memorai.domain.repository.AuthRepository;

import javax.inject.Inject;

public class AuthenticateUserUseCase {
    private final AuthRepository authRepository;

    @Inject
    public AuthenticateUserUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public User execute(String email, String password) {
        return authRepository.signIn(email, password);
    }
}
