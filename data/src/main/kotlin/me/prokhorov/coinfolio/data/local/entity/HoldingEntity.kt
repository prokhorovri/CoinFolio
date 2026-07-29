package me.prokhorov.coinfolio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "holdings")
data class HoldingEntity(
    @PrimaryKey val coinId: String,
    val quantity: BigDecimal,
)
