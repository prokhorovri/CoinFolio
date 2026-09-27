package me.prokhorov.coinfolio.domain.repository

import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.result.Resource

interface CoinRepository {
    suspend fun getTopCoins(): Resource<List<Coin>>

    /**
     * Market data for the given coins, at any market cap rank. One request covers the whole
     * list. An empty [ids] returns an empty list and makes no request.
     *
     * [forceRefresh] gets fresh prices directly from the source. It uses one request,
     * so use it only when the user asks for fresh prices.
     */
    suspend fun getCoinsByIds(
        ids: List<String>,
        forceRefresh: Boolean = false,
    ): Resource<List<Coin>>

    suspend fun getCoinDetail(coinId: String): Resource<CoinDetail>
}
