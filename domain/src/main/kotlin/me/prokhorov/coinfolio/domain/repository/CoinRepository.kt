package me.prokhorov.coinfolio.domain.repository

import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.result.Resource

interface CoinRepository {
    suspend fun getTopCoins(): Resource<List<Coin>>
}
