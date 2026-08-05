package me.prokhorov.coinfolio.domain.repository

import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.domain.model.Holding

interface PortfolioRepository {
    fun getHoldings(): Flow<List<Holding>>

    /** Emits `null` while the coin is not held, so callers can distinguish "none" from "zero". */
    fun getHolding(coinId: String): Flow<Holding?>

    /** Replaces any existing quantity for [Holding.coinId]: an absolute amount, not a delta. */
    suspend fun saveHolding(holding: Holding)

    suspend fun deleteHolding(coinId: String)
}
