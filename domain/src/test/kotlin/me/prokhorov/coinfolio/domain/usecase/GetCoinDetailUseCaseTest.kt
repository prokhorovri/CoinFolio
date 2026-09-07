package me.prokhorov.coinfolio.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.createCoinDetail
import org.junit.Test

class GetCoinDetailUseCaseTest {

    private val repository = FakeCoinRepository()
    private val getCoinDetail = GetCoinDetailUseCase(repository)

    // Two coins are in the repository. A wrong id would return the other coin, not a failure.
    // So this test proves the id really reaches the repository.
    @Test
    fun `the detail of the requested coin is returned`() = runTest {
        val ethereum = createCoinDetail("ethereum")
        repository.coinDetails = mapOf(
            "bitcoin" to Resource.Success(createCoinDetail("bitcoin")),
            "ethereum" to Resource.Success(ethereum),
        )

        val result = getCoinDetail("ethereum")

        assertThat(result).isEqualTo(Resource.Success(ethereum))
    }

    @Test
    fun `a failure keeps its error`() = runTest {
        repository.coinDetails = mapOf("bitcoin" to Resource.Failure(AppError.Network))

        val result = getCoinDetail("bitcoin")

        assertThat(result).isEqualTo(Resource.Failure(AppError.Network))
    }
}
