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
     * The same endpoint as [getTopCoins], filtered by id. So one request covers every held coin,
     * at any market cap rank. [ids] is a comma separated list and must not be empty. CoinGecko
     * reads an empty value as "no filter". It then returns the whole market list.
     */
    @GET("coins/markets")
    suspend fun getCoinsByIds(
        @Query("ids") ids: String,
        @Query("vs_currency") vsCurrency: String = "usd",
        @Query("sparkline") sparkline: Boolean = false,
    ): List<CoinDto>

    /**
     * Without these flags the endpoint returns much more: every exchange ticker, and the
     * community and developer stats. That is a few hundred KB per call. The flags keep only
     * the market data that the detail screen shows.
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
