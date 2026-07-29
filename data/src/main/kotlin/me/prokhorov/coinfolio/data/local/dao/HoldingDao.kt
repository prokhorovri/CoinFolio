package me.prokhorov.coinfolio.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity

@Dao
interface HoldingDao {
    @Query("SELECT * FROM holdings")
    fun getHoldings(): Flow<List<HoldingEntity>>

    // Replaces any existing holding for the same coin; quantity is an absolute amount, not a delta.
    @Upsert
    suspend fun upsertHolding(holding: HoldingEntity)
}
