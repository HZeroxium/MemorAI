// data/remote/api/ApiClient.java
package com.example.memorai.data.remote.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static volatile ApiService apiService;

    public static ApiService getApiService() {
        if (apiService == null) {
            synchronized (ApiClient.class) {
                if (apiService == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl("https://api.example.com/") // Update base URL as needed
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    apiService = retrofit.create(ApiService.class);
                }
            }
        }
        return apiService;
    }
}
