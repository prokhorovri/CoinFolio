package me.prokhorov.coinfolio.domain.testing

import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.repository.CoinRepository
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource

/**
 * Returns the data that a test sets. Details are stored per coin id, not in one field. So a test
 * can store two coins and ask for one. A wrong id returns the other coin. That way a test checks
 * the id itself, without asking which methods were called.
 */
class FakeCoinRepository : CoinRepository {

    var topCoins: Resource<List<Coin>> = Resource.Success(emptyList())

    var coinDetails: Map<String, Resource<CoinDetail>> = emptyMap()

    override suspend fun getTopCoins(): Resource<List<Coin>> = topCoins

    override suspend fun getCoinDetail(coinId: String): Resource<CoinDetail> {
        return coinDetails[coinId] ?: Resource.Failure(AppError.NotFound)
    }
}
