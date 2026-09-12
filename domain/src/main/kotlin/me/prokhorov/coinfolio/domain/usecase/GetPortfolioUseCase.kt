package me.prokhorov.coinfolio.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.model.Portfolio
import me.prokhorov.coinfolio.domain.model.ValuedHolding
import me.prokhorov.coinfolio.domain.result.Resource

/**
 * Builds the portfolio from the stored holdings and the prices of this moment. Any failure is a
 * [Resource.Failure], so the screen shows one error. The repository decides when the prices need
 * a new request.
 */
class GetPortfolioUseCase @Inject constructor(
    private val getHoldings: GetHoldingsUseCase,
    private val getCoinsByIds: GetCoinsByIdsUseCase,
) {
    operator fun invoke(): Flow<Resource<Portfolio>> {
        return getHoldings().map(::toPortfolio)
    }

    private suspend fun toPortfolio(
        holdingsResource: Resource<List<Holding>>,
    ): Resource<Portfolio> {
        return when (holdingsResource) {
            is Resource.Failure -> holdingsResource
            is Resource.Success -> addPrices(holdingsResource.data)
        }
    }

    /** The market request happens here. One request covers every coin the user owns. */
    private suspend fun addPrices(holdings: List<Holding>): Resource<Portfolio> {
        return when (val coinsResource = getCoinsByIds(holdings.map(Holding::coinId))) {
            is Resource.Failure -> coinsResource
            is Resource.Success -> Resource.Success(buildPortfolio(holdings, coinsResource.data))
        }
    }

    /** One row per holding. A coin the market does not know leaves that row without a price. */
    private fun buildPortfolio(holdings: List<Holding>, coins: List<Coin>): Portfolio {
        val coinsById = coins.associateBy(Coin::id)
        val rows = holdings.map { ValuedHolding(holding = it, coin = coinsById[it.coinId]) }
        return Portfolio(valuedHoldings = rows)
    }
}
