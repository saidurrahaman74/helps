package com.srcreation.smarthelp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class AqiResponse(
    val list: List<AqiData>
)

data class AqiData(
    val main: AqiMain,
    val components: Map<String, Double>
)

data class AqiMain(
    val aqi: Int // 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
)

interface EnvironmentalHealthService {
    @GET("data/2.5/air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AqiResponse

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/"
        
        fun create(): EnvironmentalHealthService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(EnvironmentalHealthService::class.java)
        }
    }
}
