package me.prokhorov.coinfolio.data.local

import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.data.local.dao.HoldingDao
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity
import javax.inject.Inject

class HoldingLocalDataSource @Inject constructor(
    private val dao: HoldingDao,
) {
    fun getHoldings(): Flow<List<HoldingEntity>> = dao.getHoldings()

    suspend fun upsertHolding(holding: HoldingEntity) = dao.upsertHolding(holding)
}
