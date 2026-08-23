package me.prokhorov.coinfolio.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import me.prokhorov.coinfolio.data.repository.CoinRepositoryImpl
import me.prokhorov.coinfolio.data.repository.PortfolioRepositoryImpl
import me.prokhorov.coinfolio.domain.repository.CoinRepository
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindCoinRepository(impl: CoinRepositoryImpl): CoinRepository

    @Binds
    @Singleton
    abstract fun bindPortfolioRepository(impl: PortfolioRepositoryImpl): PortfolioRepository
}
