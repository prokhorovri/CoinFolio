package me.prokhorov.coinfolio.domain.repository

import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.result.Resource

interface CoinRepository {
    suspend fun getTopCoins(): Resource<List<Coin>>

    /**
     * Market data for the given coins, at any market cap rank. One request covers the whole
     * list. An empty [ids] returns an empty list and makes no request.
     */
    suspend fun getCoinsByIds(ids: List<String>): Resource<List<Coin>>

    suspend fun getCoinDetail(coinId: String): Resource<CoinDetail>
}
