package me.prokhorov.coinfolio.domain.usecase

import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.model.Portfolio
import me.prokhorov.coinfolio.domain.model.ValuedHolding
import me.prokhorov.coinfolio.domain.result.Resource

/**
 * Builds the portfolio from the stored holdings and the prices of this moment. Any failure is
 * a [Resource.Failure], so the screen shows one error. The prices are requested once per set of
 * coin ids. A new quantity sends no request. Collect the flow again to load the prices again.
 */
class GetPortfolioUseCase @Inject constructor(
    private val getHoldings: GetHoldingsUseCase,
    private val getCoinsByIds: GetCoinsByIdsUseCase,
) {
    operator fun invoke(): Flow<Resource<Portfolio>> {
        return flow {
            // One cache per collector. A field here would share it between screens and retries.
            val cache = CoinCache(getCoinsByIds)

            emitAll(getHoldings().map { holdings -> loadPortfolio(holdings, cache) })
        }
    }

    /** Adds the market data to the stored holdings. The request happens here. */
    private suspend fun loadPortfolio(
        holdings: Resource<List<Holding>>,
        cache: CoinCache,
    ): Resource<Portfolio> {
        return when (holdings) {
            is Resource.Failure -> holdings

            is Resource.Success -> {
                val stored = holdings.data
                when (val answer = cache.loadCoins(stored.map(Holding::coinId).toSet())) {
                    is Resource.Failure -> answer
                    is Resource.Success -> Resource.Success(joinToPortfolio(stored, answer.data))
                }
            }
        }
    }

    /** The money part: one row per holding, and the sum of the rows that have a price. */
    private fun joinToPortfolio(holdings: List<Holding>, coinsById: Map<String, Coin>): Portfolio {
        val rows = holdings.map { ValuedHolding(holding = it, coin = coinsById[it.coinId]) }
        val total = rows.mapNotNull(ValuedHolding::value).fold(BigDecimal.ZERO, BigDecimal::add)
        return Portfolio(holdings = rows, totalValue = total)
    }
}

/** Keeps the coins of the last answer. The same set of ids is not requested twice. */
private class CoinCache(private val getCoinsByIds: GetCoinsByIdsUseCase) {

    private var loadedIds: Set<String>? = null
    private var coinsById: Map<String, Coin> = emptyMap()

    suspend fun loadCoins(ids: Set<String>): Resource<Map<String, Coin>> {
        if (ids == loadedIds) return Resource.Success(coinsById)

        return when (val answer = getCoinsByIds(ids.toList())) {
            is Resource.Failure -> answer

            is Resource.Success -> {
                coinsById = answer.data.associateBy(Coin::id)
                loadedIds = ids
                Resource.Success(coinsById)
            }
        }
    }
}
