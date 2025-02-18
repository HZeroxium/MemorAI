// di/DatabaseModule.java
package com.example.memorai.di;

import android.content.Context;

import androidx.room.Room;

import com.example.memorai.data.local.AppDatabase;
import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.dao.PhotoDao;
import com.example.memorai.data.local.dao.UserDao;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class DatabaseModule {

    @Provides
    @Singleton
    public AppDatabase provideAppDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "memorai_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    public AlbumDao provideAlbumDao(AppDatabase database) {
        return database.albumDao();
    }

    @Provides
    public PhotoDao providePhotoDao(AppDatabase database) {
        return database.photoDao();
    }

    @Provides
    public UserDao provideUserDao(AppDatabase database) {
        return database.userDao();
    }
}
