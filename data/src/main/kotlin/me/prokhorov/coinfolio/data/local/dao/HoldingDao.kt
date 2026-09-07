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

    @Query("SELECT * FROM holdings WHERE coinId = :coinId")
    fun getHolding(coinId: String): Flow<HoldingEntity?>

    @Query("DELETE FROM holdings WHERE coinId = :coinId")
    suspend fun deleteHolding(coinId: String)

    // Replaces any existing holding for the same coin. The quantity is the new total, not a change.
    @Upsert
    suspend fun upsertHolding(holding: HoldingEntity)
}
