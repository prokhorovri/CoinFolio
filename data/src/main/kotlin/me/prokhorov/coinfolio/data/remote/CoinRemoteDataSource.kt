package me.prokhorov.coinfolio.data.remote

import javax.inject.Inject
import me.prokhorov.coinfolio.data.remote.api.CoinApi
import me.prokhorov.coinfolio.data.remote.api.CoinApiConfig
import me.prokhorov.coinfolio.data.remote.dto.CoinDetailDto
import me.prokhorov.coinfolio.data.remote.dto.CoinDto

class CoinRemoteDataSource @Inject constructor(
    private val api: CoinApi,
) {
    suspend fun getTopCoins(): List<CoinDto> = api.getTopCoins()

    suspend fun getCoinsByIds(ids: List<String>): List<CoinDto> {
        return api.getCoinsByIds(ids = ids.joinToString(CoinApiConfig.ID_SEPARATOR))
    }

    suspend fun getCoinDetail(coinId: String): CoinDetailDto = api.getCoinDetail(id = coinId)
}
