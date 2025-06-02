package com.example.walk2lose

import retrofit2.http.GET
import retrofit2.http.Query

interface DirectionsApi {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("mode") mode: String = "walking", // walking için
        @Query("key") apiKey: String
    ): DirectionsResponse
}