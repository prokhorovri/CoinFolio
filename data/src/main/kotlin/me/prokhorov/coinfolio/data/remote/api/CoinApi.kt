package me.prokhorov.coinfolio.data.remote.api

import me.prokhorov.coinfolio.data.remote.dto.CoinDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinApi {
    @GET("coins/markets")
    suspend fun getTopCoins(
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("order") order: String = "market_cap_desc",
        @Query("per_page") perPage: Int = 50,
        @Query("page") page: Int = 1,
        @Query("sparkline") sparkline: Boolean = true,
    ): List<CoinDto>
}
