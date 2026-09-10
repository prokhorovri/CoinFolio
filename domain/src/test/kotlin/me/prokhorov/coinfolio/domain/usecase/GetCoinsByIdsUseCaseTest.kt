package me.prokhorov.coinfolio.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.createCoin
import org.junit.Test

class GetCoinsByIdsUseCaseTest {

    private val repository = FakeCoinRepository()
    private val getCoinsByIds = GetCoinsByIdsUseCase(repository)

    // The fake knows two coins and answers with the ones it was asked for. So this test also
    // proves the ids reach the repository.
    @Test
    fun `only the requested coins are returned`() = runTest {
        repository.marketCoins = mapOf(
            "bitcoin" to createCoin("bitcoin"),
            "ethereum" to createCoin("ethereum"),
        )

        val result = getCoinsByIds(listOf("ethereum"))

        assertThat(result).isEqualTo(Resource.Success(listOf(createCoin("ethereum"))))
    }

    @Test
    fun `an empty list is a success, not a failure`() = runTest {
        val result = getCoinsByIds(emptyList())

        assertThat(result).isEqualTo(Resource.Success(emptyList<Any>()))
    }

    @Test
    fun `a failure keeps its error`() = runTest {
        repository.marketError = AppError.RateLimited

        val result = getCoinsByIds(listOf("bitcoin"))

        assertThat(result).isEqualTo(Resource.Failure(AppError.RateLimited))
    }
}
