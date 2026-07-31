package me.prokhorov.coinfolio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.prokhorov.coinfolio.data.local.HoldingLocalDataSource
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity
import me.prokhorov.coinfolio.data.mapper.toDomain
import me.prokhorov.coinfolio.data.mapper.toEntity
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    private val holdingLocalDataSource: HoldingLocalDataSource,
) : PortfolioRepository {
    override fun getHoldings(): Flow<List<Holding>> {
        return holdingLocalDataSource
            .getHoldings()
            .map { entities -> entities.map(HoldingEntity::toDomain) }
    }

    override fun getHolding(coinId: String): Flow<Holding?> {
        return holdingLocalDataSource
            .getHolding(coinId)
            .map { entity -> entity?.toDomain() }
    }

    override suspend fun saveHolding(holding: Holding) {
        holdingLocalDataSource.upsertHolding(holding.toEntity())
    }

    override suspend fun deleteHolding(coinId: String) {
        holdingLocalDataSource.deleteHolding(coinId)
    }
}
