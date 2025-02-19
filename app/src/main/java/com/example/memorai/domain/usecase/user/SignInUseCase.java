// domain/usecase/user/SignInUseCase.java
package com.example.memorai.domain.usecase.user;

import com.example.memorai.domain.model.User;
import com.example.memorai.domain.repository.AuthRepository;

import javax.inject.Inject;

public class SignInUseCase {
    private final AuthRepository authRepository;

    @Inject
    public SignInUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public User execute(String email, String password) {
        return authRepository.signIn(email, password);
    }
}
