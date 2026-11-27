package com.worthour.data

import retrofit2.http.GET

interface FrankfurterService {
    @GET("v2/rate/usd/krw")
    suspend fun getUsdToKrwRate(): FrankfurterResponse
}
