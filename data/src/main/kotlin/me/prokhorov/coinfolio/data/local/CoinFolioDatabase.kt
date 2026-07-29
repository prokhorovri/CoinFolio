package me.prokhorov.coinfolio.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import me.prokhorov.coinfolio.data.local.converter.BigDecimalConverter
import me.prokhorov.coinfolio.data.local.dao.HoldingDao
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity

@Database(entities = [HoldingEntity::class], version = 1, exportSchema = false)
@TypeConverters(BigDecimalConverter::class)
abstract class CoinFolioDatabase : RoomDatabase() {
    abstract fun holdingDao(): HoldingDao

    companion object {
        const val DATABASE_NAME = "coinfolio.db"
    }
}
