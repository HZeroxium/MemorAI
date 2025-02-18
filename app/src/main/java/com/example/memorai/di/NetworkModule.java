// di/NetworkModule.java
package com.example.memorai.di;

import com.example.memorai.data.remote.api.ApiClient;
import com.example.memorai.data.remote.api.ApiService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public ApiService provideApiService() {
        return ApiClient.getApiService();
    }
}
