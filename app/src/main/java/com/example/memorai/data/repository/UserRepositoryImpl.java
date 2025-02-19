// data/repository/UserRepositoryImpl.java
package com.example.memorai.data.repository;

import com.example.memorai.data.local.dao.UserDao;
import com.example.memorai.data.local.entity.UserEntity;
import com.example.memorai.data.mappers.UserMapper;
import com.example.memorai.domain.model.User;
import com.example.memorai.domain.repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepositoryImpl implements UserRepository {

    private final UserDao userDao;

    @Inject
    public UserRepositoryImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User getUserById(String userId) {
        UserEntity entity = userDao.getUserById(userId);
        return entity != null ? UserMapper.toDomain(entity) : null;
    }

    @Override
    public void updateUser(User user) {
        userDao.updateUser(UserMapper.fromDomain(user));
    }
}
