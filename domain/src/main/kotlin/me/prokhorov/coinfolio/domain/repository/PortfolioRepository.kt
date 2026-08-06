package me.prokhorov.coinfolio.domain.repository

import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.result.Resource

interface PortfolioRepository {
    fun getHoldings(): Flow<Resource<List<Holding>>>

    /** Emits `null` while the coin is not held, so callers can distinguish "none" from "zero". */
    fun getHolding(coinId: String): Flow<Resource<Holding?>>

    /** Replaces any existing quantity for [Holding.coinId]: an absolute amount, not a delta. */
    suspend fun saveHolding(holding: Holding): Resource<Unit>

    suspend fun deleteHolding(coinId: String): Resource<Unit>
}
