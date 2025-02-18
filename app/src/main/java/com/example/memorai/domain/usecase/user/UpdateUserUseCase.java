// domain/usecase/user/UpdateUserUseCase.java
package com.example.memorai.domain.usecase.user;

import com.example.memorai.domain.model.User;
import com.example.memorai.domain.repository.UserRepository;

import javax.inject.Inject;

public class UpdateUserUseCase {
    private final UserRepository userRepository;

    @Inject
    public UpdateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void execute(User user) {
        userRepository.updateUser(user);
    }
}
