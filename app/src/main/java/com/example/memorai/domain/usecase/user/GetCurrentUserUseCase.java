// domain/usecase/user/GetCurrentUserUseCase.java
package com.example.memorai.domain.usecase.user;

import com.example.memorai.domain.model.User;
import com.example.memorai.domain.repository.AuthRepository;

import javax.inject.Inject;

public class GetCurrentUserUseCase {
    private final AuthRepository authRepository;

    @Inject
    public GetCurrentUserUseCase(AuthRepository authRepository) {
        this.authRepository = authRepository;
    }

    public User execute() {
        return authRepository.getCurrentUser();
    }
}
