package me.prokhorov.coinfolio.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.time.TimeSource

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Monotonic measures elapsed time only. A wall clock can jump when the user or the network
    // changes the device time, and then an old cached answer would look fresh.
    @Provides
    @Singleton
    fun provideTimeSource(): TimeSource = TimeSource.Monotonic
}
