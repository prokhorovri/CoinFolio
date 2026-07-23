package me.prokhorov.coinfolio.domain.repository

import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.Resource

interface CoinRepository {
    suspend fun getTopCoins(): Resource<List<Coin>>
}
