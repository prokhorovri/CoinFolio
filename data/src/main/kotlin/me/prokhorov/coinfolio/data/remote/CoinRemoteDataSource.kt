package me.prokhorov.coinfolio.data.remote

import me.prokhorov.coinfolio.data.remote.api.CoinApi
import me.prokhorov.coinfolio.data.remote.dto.CoinDto
import javax.inject.Inject

class CoinRemoteDataSource @Inject constructor(
    private val api: CoinApi,
) {
    suspend fun getTopCoins(): List<CoinDto> = api.getTopCoins()
}
