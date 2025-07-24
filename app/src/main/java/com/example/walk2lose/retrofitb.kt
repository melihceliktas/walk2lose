package com.example.walk2lose

import com.google.android.gms.common.api.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface OverpassApi {
    @POST("interpreter")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    suspend fun getNearbyRoads(@Body query: RequestBody): retrofit2.Response<String>
}

val retrofit = Retrofit.Builder()
    .baseUrl("https://overpass-api.de/api/")
    .addConverterFactory(ScalarsConverterFactory.create()) // Scalar converter ekli
    .build()

val api = retrofit.create(OverpassApi::class.java)

suspend fun isRoadNearby(lat: Double, lng: Double): Boolean {
    val query = """
        [out:json];
        way["highway"](around:100, $lat, $lng);
        out body;
    """.trimIndent()

    val body = query.toRequestBody("application/x-www-form-urlencoded".toMediaType())
    val response = api.getNearbyRoads(body)

    if (response.isSuccessful) {
        val result = response.body()
        return result?.contains("\"elements\"") == true && !result.contains("\"elements\":[]")
    }
    return false
}
