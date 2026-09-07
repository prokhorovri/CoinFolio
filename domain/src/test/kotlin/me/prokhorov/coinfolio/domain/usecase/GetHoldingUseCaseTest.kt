package me.prokhorov.coinfolio.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakePortfolioRepository
import me.prokhorov.coinfolio.domain.testing.createHolding
import org.junit.Test

class GetHoldingUseCaseTest {

    private val repository = FakePortfolioRepository()
    private val getHolding = GetHoldingUseCase(repository)

    @Test
    fun `a stored holding is emitted`() = runTest {
        repository.saveHolding(createHolding("bitcoin", "0.5"))

        val result = getHolding("bitcoin").first()

        assertThat(result).isEqualTo(Resource.Success(createHolding("bitcoin", "0.5")))
    }

    // "Not held" and "a quantity of zero" must not look the same to the caller.
    @Test
    fun `a coin that is not held emits success with null`() = runTest {
        repository.saveHolding(createHolding("bitcoin", "0.5"))

        val result = getHolding("ethereum").first()

        assertThat(result).isEqualTo(Resource.Success(null))
    }

    @Test
    fun `a failure keeps its error`() = runTest {
        repository.error = AppError.Storage

        val result = getHolding("bitcoin").first()

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }
}
