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
     * Without these flags the endpoint also returns every exchange ticker and the community and
     * developer stats, a few hundred KB per call. The flags keep only the market data that the
     * detail screen shows.
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
