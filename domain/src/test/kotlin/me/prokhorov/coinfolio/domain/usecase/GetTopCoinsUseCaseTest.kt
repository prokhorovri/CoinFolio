package me.prokhorov.coinfolio.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.createCoin
import org.junit.Test

class GetTopCoinsUseCaseTest {

    private val repository = FakeCoinRepository()
    private val getTopCoins = GetTopCoinsUseCase(repository)

    @Test
    fun `the coins from the repository are returned unchanged`() = runTest {
        val coins = listOf(createCoin("bitcoin"), createCoin("ethereum"))
        repository.topCoins = Resource.Success(coins)

        val result = getTopCoins()

        assertThat(result).isEqualTo(Resource.Success(coins))
    }

    @Test
    fun `an empty list is a success, not a failure`() = runTest {
        repository.topCoins = Resource.Success(emptyList())

        val result = getTopCoins()

        assertThat(result).isEqualTo(Resource.Success(emptyList<Any>()))
    }

    @Test
    fun `a failure keeps its error`() = runTest {
        repository.topCoins = Resource.Failure(AppError.RateLimited)

        val result = getTopCoins()

        assertThat(result).isEqualTo(Resource.Failure(AppError.RateLimited))
    }
}
