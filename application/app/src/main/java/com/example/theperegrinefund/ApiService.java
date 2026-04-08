package com.example.theperegrinefund;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/send") // correspond à ton endpoint Spring Boot
    Call<Void> sendEncryptedMessage(@Body String encryptedMessage);
}
