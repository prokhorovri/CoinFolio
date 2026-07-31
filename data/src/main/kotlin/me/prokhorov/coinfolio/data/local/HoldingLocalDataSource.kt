package me.prokhorov.coinfolio.data.local

import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.data.local.dao.HoldingDao
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity
import javax.inject.Inject

class HoldingLocalDataSource @Inject constructor(
    private val dao: HoldingDao,
) {
    fun getHoldings(): Flow<List<HoldingEntity>> = dao.getHoldings()

    fun getHolding(coinId: String): Flow<HoldingEntity?> = dao.getHolding(coinId)

    suspend fun upsertHolding(holding: HoldingEntity) = dao.upsertHolding(holding)

    suspend fun deleteHolding(coinId: String) = dao.deleteHolding(coinId)
}
