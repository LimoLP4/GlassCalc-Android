package com.limonatalp4.glasscalcproject.data.remote

import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = false)
data class FrankfurterRatesResponse(
    val amount: Double = 1.0,
    val base: String = "EUR",
    val date: String = "",
    val rates: Map<String, Double> = emptyMap()
)

interface FrankfurterApiService {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") base: String = "EUR",
        @Query("to") symbols: String? = null
    ): FrankfurterRatesResponse

    companion object {
        const val BASE_URL = "https://api.frankfurter.app/"
    }
}
