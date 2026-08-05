package me.prokhorov.coinfolio.data.remote.api

import me.prokhorov.coinfolio.data.remote.dto.CoinDetailDto
import me.prokhorov.coinfolio.data.remote.dto.CoinDto
import retrofit2.http.GET
import retrofit2.http.Path
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

    /**
     * The bare endpoint also returns every exchange ticker plus community and developer stats,
     * hundreds of KB per call. The flags below trim it to the market data the detail screen shows.
     */
    @GET("coins/{id}")
    suspend fun getCoinDetail(
        @Path("id") id: String,
        @Query("localization") localization: Boolean = false,
        @Query("tickers") tickers: Boolean = false,
        @Query("market_data") marketData: Boolean = true,
        @Query("community_data") communityData: Boolean = false,
        @Query("developer_data") developerData: Boolean = false,
        @Query("sparkline") sparkline: Boolean = false,
    ): CoinDetailDto
}
