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
    private val localDataSource: HoldingLocalDataSource,
) : PortfolioRepository {
    override fun getHoldings(): Flow<List<Holding>> {
        return localDataSource
            .getHoldings()
            .map { entities -> entities.map(HoldingEntity::toDomain) }
    }

    override suspend fun addHolding(holding: Holding) {
        localDataSource.upsertHolding(holding.toEntity())
    }
}
