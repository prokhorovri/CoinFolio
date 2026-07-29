package me.prokhorov.coinfolio.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import me.prokhorov.coinfolio.data.local.CoinFolioDatabase
import me.prokhorov.coinfolio.data.local.dao.HoldingDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideCoinFolioDatabase(@ApplicationContext context: Context): CoinFolioDatabase {
        return Room.databaseBuilder(
            context,
            CoinFolioDatabase::class.java,
            CoinFolioDatabase.DATABASE_NAME,
        ).build()
    }

    @Provides
    @Singleton
    fun provideHoldingDao(database: CoinFolioDatabase): HoldingDao = database.holdingDao()
}
