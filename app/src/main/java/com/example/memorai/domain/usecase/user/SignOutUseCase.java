// domain/usecase/user/SignOutUseCase.java
package com.example.memorai.domain.usecase.user;

import com.example.memorai.domain.repository.AuthRepository;

import javax.inject.Inject;

public class SignOutUseCase {
    private final AuthRepository authRepository;

    @Inject
    public SignOutUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public void execute() {
        authRepository.signOut();
    }
}
