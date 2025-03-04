// di/UseCaseModule.java
package com.example.memorai.di;

import com.example.memorai.domain.repository.AlbumRepository;
import com.example.memorai.domain.repository.AuthRepository;
import com.example.memorai.domain.repository.CloudSyncRepository;
import com.example.memorai.domain.repository.PhotoRepository;
import com.example.memorai.domain.repository.SettingsRepository;
import com.example.memorai.domain.repository.UserRepository;
import com.example.memorai.domain.usecase.album.CreateAlbumUseCase;
import com.example.memorai.domain.usecase.album.GetAlbumByIdUseCase;
import com.example.memorai.domain.usecase.album.GetAlbumsUseCase;
import com.example.memorai.domain.usecase.album.SearchAlbumsUseCase;
import com.example.memorai.domain.usecase.album.SortAlbumsUseCase;
import com.example.memorai.domain.usecase.cloud.SyncAlbumsUseCase;
import com.example.memorai.domain.usecase.cloud.SyncPhotosUseCase;
import com.example.memorai.domain.usecase.cloud.UploadPhotoToCloudUseCase;
import com.example.memorai.domain.usecase.photo.AddPhotoUseCase;
import com.example.memorai.domain.usecase.photo.DeletePhotoUseCase;
import com.example.memorai.domain.usecase.photo.EditPhotoUseCase;
import com.example.memorai.domain.usecase.photo.GetPhotosByAlbumUseCase;
import com.example.memorai.domain.usecase.photo.SearchPhotosUseCase;
import com.example.memorai.domain.usecase.photo.SortPhotosUseCase;
import com.example.memorai.domain.usecase.settings.GetAppSettingsUseCase;
import com.example.memorai.domain.usecase.settings.UpdateAppSettingsUseCase;
import com.example.memorai.domain.usecase.user.GetCurrentUserUseCase;
import com.example.memorai.domain.usecase.user.SignInUseCase;
import com.example.memorai.domain.usecase.user.SignOutUseCase;
import com.example.memorai.domain.usecase.user.UpdateUserUseCase;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class UseCaseModule {

    @Provides
    @Singleton
    public CreateAlbumUseCase provideCreateAlbumUseCase(AlbumRepository albumRepository) {
        return new CreateAlbumUseCase(albumRepository);
    }

    @Provides
    @Singleton
    public GetAlbumsUseCase provideGetAlbumsUseCase(AlbumRepository albumRepository) {
        return new GetAlbumsUseCase(albumRepository);
    }

    @Provides
    @Singleton
    public GetAlbumByIdUseCase provideGetAlbumByIdUseCase(AlbumRepository albumRepository) {
        return new GetAlbumByIdUseCase(albumRepository);
    }

    @Provides
    @Singleton
    public SearchAlbumsUseCase provideSearchAlbumsUseCase(AlbumRepository albumRepository) {
        return new SearchAlbumsUseCase(albumRepository);
    }

    @Provides
    @Singleton
    public SortAlbumsUseCase provideSortAlbumsUseCase(AlbumRepository albumRepository) {
        return new SortAlbumsUseCase(albumRepository);
    }

    @Provides
    @Singleton
    public AddPhotoUseCase provideAddPhotoUseCase(PhotoRepository photoRepository) {
        return new AddPhotoUseCase(photoRepository);
    }

    @Provides
    @Singleton
    public EditPhotoUseCase provideEditPhotoUseCase(PhotoRepository photoRepository) {
        return new EditPhotoUseCase(photoRepository);
    }

    @Provides
    @Singleton
    public DeletePhotoUseCase provideDeletePhotoUseCase(PhotoRepository photoRepository) {
        return new DeletePhotoUseCase(photoRepository);
    }

    @Provides
    @Singleton
    public GetPhotosByAlbumUseCase provideGetPhotosByAlbumUseCase(PhotoRepository photoRepository) {
        return new GetPhotosByAlbumUseCase(photoRepository);
    }

    @Provides
    @Singleton
    public SearchPhotosUseCase provideSearchPhotosUseCase(PhotoRepository photoRepository) {
        return new SearchPhotosUseCase(photoRepository);
    }

    @Provides
    @Singleton
    public SortPhotosUseCase provideSortPhotosUseCase(PhotoRepository photoRepository) {
        return new SortPhotosUseCase(photoRepository);
    }

    @Provides
    @Singleton
    public SyncAlbumsUseCase provideSyncAlbumsUseCase(CloudSyncRepository cloudSyncRepository) {
        return new SyncAlbumsUseCase(cloudSyncRepository);
    }

    @Provides
    @Singleton
    public SyncPhotosUseCase provideSyncPhotosUseCase(CloudSyncRepository cloudSyncRepository) {
        return new SyncPhotosUseCase(cloudSyncRepository);
    }

    @Provides
    @Singleton
    public UploadPhotoToCloudUseCase provideUploadPhotoToCloudUseCase(CloudSyncRepository cloudSyncRepository) {
        return new UploadPhotoToCloudUseCase(cloudSyncRepository);
    }

    @Provides
    @Singleton
    public GetAppSettingsUseCase provideGetAppSettingsUseCase(SettingsRepository settingsRepository) {
        return new GetAppSettingsUseCase(settingsRepository);
    }

    @Provides
    @Singleton
    public UpdateAppSettingsUseCase provideUpdateAppSettingsUseCase(SettingsRepository settingsRepository) {
        return new UpdateAppSettingsUseCase(settingsRepository);
    }

    @Provides
    @Singleton
    public SignInUseCase provideSignInUseCase(AuthRepository authRepository) {
        return new SignInUseCase(authRepository);
    }

    @Provides
    @Singleton
    public SignOutUseCase provideSignOutUseCase(AuthRepository authRepository) {
        return new SignOutUseCase(authRepository);
    }

    @Provides
    @Singleton
    public GetCurrentUserUseCase provideGetCurrentUserUseCase(AuthRepository authRepository) {
        return new GetCurrentUserUseCase(authRepository);
    }

    @Provides
    @Singleton
    public UpdateUserUseCase provideUpdateUserUseCase(UserRepository userRepository) {
        return new UpdateUserUseCase(userRepository);
    }
}
