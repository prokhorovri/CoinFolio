package me.prokhorov.coinfolio.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeHoldingRepository
import me.prokhorov.coinfolio.domain.testing.createHolding
import org.junit.Test

class GetHoldingsUseCaseTest {

    private val repository = FakeHoldingRepository()
    private val getHoldings = GetHoldingsUseCase(repository)

    @Test
    fun `no stored holdings emits an empty list, not a failure`() = runTest {
        val result = getHoldings().first()

        assertThat(result).isEqualTo(Resource.Success(emptyList<Any>()))
    }

    @Test
    fun `every stored holding is emitted`() = runTest {
        repository.saveHolding(createHolding("bitcoin", "0.5"))
        repository.saveHolding(createHolding("ethereum", "2"))

        val result = getHoldings().first()

        val expected = listOf(
            createHolding("bitcoin", "0.5"),
            createHolding("ethereum", "2"),
        )
        assertThat(result).isEqualTo(Resource.Success(expected))
    }

    @Test
    fun `a failure keeps its error`() = runTest {
        repository.error = AppError.Storage

        val result = getHoldings().first()

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }
}
