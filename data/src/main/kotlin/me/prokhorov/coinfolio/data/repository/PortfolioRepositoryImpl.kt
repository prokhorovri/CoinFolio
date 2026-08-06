package me.prokhorov.coinfolio.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.prokhorov.coinfolio.data.local.HoldingLocalDataSource
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity
import me.prokhorov.coinfolio.data.local.util.asResource
import me.prokhorov.coinfolio.data.local.util.dbCall
import me.prokhorov.coinfolio.data.mapper.toDomain
import me.prokhorov.coinfolio.data.mapper.toEntity
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository
import me.prokhorov.coinfolio.domain.result.Resource
import javax.inject.Inject

class PortfolioRepositoryImpl @Inject constructor(
    private val holdingLocalDataSource: HoldingLocalDataSource,
) : PortfolioRepository {
    override fun getHoldings(): Flow<Resource<List<Holding>>> {
        return holdingLocalDataSource
            .getHoldings()
            .map { entities -> entities.map(HoldingEntity::toDomain) }
            .asResource()
    }

    override fun getHolding(coinId: String): Flow<Resource<Holding?>> {
        return holdingLocalDataSource
            .getHolding(coinId)
            .map { entity -> entity?.toDomain() }
            .asResource()
    }

    override suspend fun saveHolding(holding: Holding): Resource<Unit> {
        return dbCall { holdingLocalDataSource.upsertHolding(holding.toEntity()) }
    }

    override suspend fun deleteHolding(coinId: String): Resource<Unit> {
        return dbCall { holdingLocalDataSource.deleteHolding(coinId) }
    }
}
