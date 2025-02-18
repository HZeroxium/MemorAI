// data/local/AppDatabase.java
package com.example.memorai.data.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.dao.PhotoDao;
import com.example.memorai.data.local.dao.UserDao;
import com.example.memorai.data.local.entity.AlbumEntity;
import com.example.memorai.data.local.entity.PhotoEntity;
import com.example.memorai.data.local.entity.UserEntity;

@Database(entities = {AlbumEntity.class, PhotoEntity.class, UserEntity.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract AlbumDao albumDao();
    public abstract PhotoDao photoDao();

    public abstract UserDao userDao();
}

