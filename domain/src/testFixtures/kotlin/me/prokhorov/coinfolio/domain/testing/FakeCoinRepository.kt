package me.prokhorov.coinfolio.domain.testing

import kotlinx.coroutines.CompletableDeferred
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

    /** Coins the market call knows, by id. An id that is not here is left out of the answer. */
    var marketCoins: Map<String, Coin> = emptyMap()

    /** Set to make [getCoinsByIds] fail. */
    var marketError: AppError? = null

    /** The `forceRefresh` value of the last [getCoinsByIds] call. */
    var lastForceRefresh: Boolean? = null
        private set

    /**
     * Set this to pause every market call until the test completes it. A call reads its answer
     * before the pause. So a test can change the data and see which call used the old one.
     */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun getTopCoins(): Resource<List<Coin>> {
        val answer = topCoins
        gate?.await()
        return answer
    }

    override suspend fun getCoinsByIds(
        ids: List<String>,
        forceRefresh: Boolean,
    ): Resource<List<Coin>> {
        lastForceRefresh = forceRefresh

        val failure = marketError?.let { Resource.Failure(it) }
        val answer = failure ?: Resource.Success(ids.mapNotNull { marketCoins[it] })

        gate?.await()

        return answer
    }

    override suspend fun getCoinDetail(coinId: String): Resource<CoinDetail> {
        return coinDetails[coinId] ?: Resource.Failure(AppError.NotFound)
    }
}
