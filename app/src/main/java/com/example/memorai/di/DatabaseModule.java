// di/DatabaseModule.java
package com.example.memorai.di;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.room.Room;

import com.example.memorai.data.local.AppDatabase;
import com.example.memorai.data.local.dao.AlbumDao;
import com.example.memorai.data.local.dao.PhotoDao;
import com.example.memorai.data.local.dao.UserDao;
import com.example.memorai.data.repository.AlbumRepositoryImpl;
import com.example.memorai.data.repository.AuthRepositoryImpl;
import com.example.memorai.data.repository.CloudSyncRepositoryImpl;
import com.example.memorai.data.repository.PhotoRepositoryImpl;
import com.example.memorai.data.repository.SettingsRepositoryImpl;
import com.example.memorai.data.repository.UserRepositoryImpl;
import com.example.memorai.domain.repository.AlbumRepository;
import com.example.memorai.domain.repository.AuthRepository;
import com.example.memorai.domain.repository.CloudSyncRepository;
import com.example.memorai.domain.repository.PhotoRepository;
import com.example.memorai.domain.repository.SettingsRepository;
import com.example.memorai.domain.repository.UserRepository;

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

    @Provides
    public AlbumRepository provideAlbumRepository(AlbumDao albumDao) {
        return new AlbumRepositoryImpl(albumDao);
    }

    @Provides
    public PhotoRepository providePhotoRepository(PhotoDao photoDao) {
        return new PhotoRepositoryImpl(photoDao);
    }

    @Provides
    public UserRepository provideUserRepository(UserDao userDao) {
        return new UserRepositoryImpl(userDao);
    }

    @Provides
    public SettingsRepository provideSettingsRepository(SharedPreferences sharedPreferences) {
        return new SettingsRepositoryImpl(sharedPreferences);
    }

    @Provides
    public CloudSyncRepository provideCloudSyncRepository() {
        return new CloudSyncRepositoryImpl();
    }

    @Provides
    public AuthRepository provideTagRepository() {
        return new AuthRepositoryImpl();
    }
}
