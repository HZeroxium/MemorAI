// data/remote/api/ApiService.java
package com.example.memorai.data.remote.api;

import com.example.memorai.data.remote.dto.AlbumDto;
import com.example.memorai.data.remote.dto.PhotoDto;
import com.example.memorai.data.remote.dto.UserDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface ApiService {
    // Albums API
    @GET("albums")
    Call<List<AlbumDto>> getAlbums();

    @GET("albums/{id}")
    Call<AlbumDto> getAlbumById(@Path("id") String albumId);

    @POST("albums")
    Call<Void> createAlbum(@Body AlbumDto album);

    @PUT("albums/{id}")
    Call<Void> updateAlbum(@Path("id") String albumId, @Body AlbumDto album);

    @DELETE("albums/{id}")
    Call<Void> deleteAlbum(@Path("id") String albumId);

    // Photos API
    @GET("albums/{albumId}/photos")
    Call<List<PhotoDto>> getPhotosByAlbum(@Path("albumId") String albumId);

    @GET("photos/{id}")
    Call<PhotoDto> getPhotoById(@Path("id") String photoId);

    @POST("photos")
    Call<Void> createPhoto(@Body PhotoDto photo);

    @PUT("photos/{id}")
    Call<Void> updatePhoto(@Path("id") String photoId, @Body PhotoDto photo);

    @DELETE("photos/{id}")
    Call<Void> deletePhoto(@Path("id") String photoId);

    // Users API
    @GET("users/{id}")
    Call<UserDto> getUserById(@Path("id") String userId);

    @PUT("users/{id}")
    Call<Void> updateUser(@Path("id") String userId, @Body UserDto user);
}
