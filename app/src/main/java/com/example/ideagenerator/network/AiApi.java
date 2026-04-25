package com.example.ideagenerator.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AiApi {

    @POST("chat/completions")
    Call<AiResponse> generateIdea(
            @Header("Authorization") String authHeader,
            @Body AiRequest request
    );
}
