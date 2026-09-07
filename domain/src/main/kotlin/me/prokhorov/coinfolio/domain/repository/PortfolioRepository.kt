package me.prokhorov.coinfolio.domain.repository

import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.result.Resource

interface PortfolioRepository {
    fun getHoldings(): Flow<Resource<List<Holding>>>

    /** Emits `null` when the coin is not held. That is not the same as a quantity of zero. */
    fun getHolding(coinId: String): Flow<Resource<Holding?>>

    /** Replaces any existing quantity for [Holding.coinId]. Pass the new total, not a change. */
    suspend fun saveHolding(holding: Holding): Resource<Unit>

    suspend fun deleteHolding(coinId: String): Resource<Unit>
}
