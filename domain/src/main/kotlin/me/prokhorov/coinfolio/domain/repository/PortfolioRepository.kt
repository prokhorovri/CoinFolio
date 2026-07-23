package me.prokhorov.coinfolio.domain.repository

import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.domain.model.Holding

interface PortfolioRepository {
    fun getHoldings(): Flow<List<Holding>>
    suspend fun addHolding(holding: Holding)
}
