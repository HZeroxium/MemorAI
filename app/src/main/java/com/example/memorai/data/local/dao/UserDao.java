// data/local/dao/UserDao.java
package com.example.memorai.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.memorai.data.local.entity.UserEntity;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user WHERE id = :userId LIMIT 1")
    UserEntity getUserById(String userId);

    @Insert
    void insertUser(UserEntity user);

    @Update
    void updateUser(UserEntity user);
}
