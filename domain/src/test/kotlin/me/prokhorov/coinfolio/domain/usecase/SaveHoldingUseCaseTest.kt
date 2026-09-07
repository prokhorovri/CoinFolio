package me.prokhorov.coinfolio.domain.usecase

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakePortfolioRepository
import me.prokhorov.coinfolio.domain.testing.createHolding
import org.junit.Test

/**
 * The only use case with its own rule. A quantity of zero deletes the holding. It does not
 * store a row with zero.
 *
 * Every check here uses isEqualTo, not isEqualByComparingTo. The fake returns the same
 * scale it was given. So exact equality is the stronger check.
 */
class SaveHoldingUseCaseTest {

    private val repository = FakePortfolioRepository()
    private val saveHolding = SaveHoldingUseCase(repository)

    @Test
    fun `a positive quantity is stored`() = runTest {
        val result = saveHolding(createHolding("bitcoin", "0.5"))

        assertThat(result).isEqualTo(Resource.Success(Unit))
        val stored = repository.getHolding("bitcoin").first()
        assertThat(stored).isEqualTo(Resource.Success(createHolding("bitcoin", "0.5")))
    }

    // The quantity is the full amount, not a change. To add two more coins, the caller must
    // calculate the new total first.
    @Test
    fun `saving again replaces the quantity instead of adding to it`() = runTest {
        saveHolding(createHolding("bitcoin", "1"))

        saveHolding(createHolding("bitcoin", "3"))

        val stored = repository.getHolding("bitcoin").first()
        assertThat(stored).isEqualTo(Resource.Success(createHolding("bitcoin", "3")))
    }

    @Test
    fun `a quantity of zero deletes the holding instead of storing a zero row`() = runTest {
        saveHolding(createHolding("bitcoin", "0.5"))

        val result = saveHolding(createHolding("bitcoin", "0"))

        assertThat(result).isEqualTo(Resource.Success(Unit))
        val stored = repository.getHolding("bitcoin").first()
        assertThat(stored).isEqualTo(Resource.Success(null))
    }

    // The code uses signum() instead of comparing with BigDecimal.ZERO. So the scale does
    // not change the answer: "0.000" is still zero.
    @Test
    fun `a zero written with decimals deletes the holding too`() = runTest {
        saveHolding(createHolding("bitcoin", "0.5"))

        saveHolding(createHolding("bitcoin", "0.000"))

        val stored = repository.getHolding("bitcoin").first()
        assertThat(stored).isEqualTo(Resource.Success(null))
    }

    @Test
    fun `a quantity of zero on a coin that is not held still succeeds`() = runTest {
        val result = saveHolding(createHolding("ethereum", "0"))

        assertThat(result).isEqualTo(Resource.Success(Unit))
    }

    @Test
    fun `only the coin being saved is changed`() = runTest {
        saveHolding(createHolding("bitcoin", "0.5"))
        saveHolding(createHolding("ethereum", "2"))

        saveHolding(createHolding("bitcoin", "0"))

        val stored = repository.getHolding("ethereum").first()
        assertThat(stored).isEqualTo(Resource.Success(createHolding("ethereum", "2")))
    }

    @Test
    fun `a failure while saving keeps its error`() = runTest {
        repository.error = AppError.Storage

        val result = saveHolding(createHolding("bitcoin", "0.5"))

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }

    @Test
    fun `a failure while deleting keeps its error`() = runTest {
        repository.error = AppError.Storage

        val result = saveHolding(createHolding("bitcoin", "0"))

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }
}
