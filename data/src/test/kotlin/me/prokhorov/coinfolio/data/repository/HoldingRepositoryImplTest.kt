package me.prokhorov.coinfolio.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.data.local.HoldingLocalDataSource
import me.prokhorov.coinfolio.data.testing.InMemoryDatabaseRule
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.createHolding
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val COIN_ID = "bitcoin"

/**
 * This test uses a real in-memory database. It checks the mappers, DAO and `Resource`
 * wrapping together, like the app does.
 */
@RunWith(RobolectricTestRunner::class)
class HoldingRepositoryImplTest {

    @get:Rule
    val databaseRule = InMemoryDatabaseRule()

    private val repository: HoldingRepositoryImpl by lazy {
        HoldingRepositoryImpl(HoldingLocalDataSource(databaseRule.holdingDao))
    }

    @Test
    fun `a saved holding is read back as a domain holding`() = runTest {
        val saveResult = repository.saveHolding(createHolding(COIN_ID, "0.5"))

        assertThat(saveResult).isEqualTo(Resource.Success(Unit))
        assertThat(repository.getHolding(COIN_ID).first()).isEqualTo(
            Resource.Success(
                Holding(COIN_ID, BigDecimal("0.5")),
            ),
        )
    }

    @Test
    fun `an unknown coin reads as a success holding null`() = runTest {
        assertThat(repository.getHolding(COIN_ID).first()).isEqualTo(Resource.Success(null))
    }

    @Test
    fun `saving the same coin again replaces the quantity`() = runTest {
        repository.saveHolding(createHolding(COIN_ID, "0.5"))

        repository.saveHolding(createHolding(COIN_ID, "1.5"))

        assertThat(repository.getHoldings().first()).isEqualTo(
            Resource.Success(listOf(Holding(COIN_ID, BigDecimal("1.5")))),
        )
    }

    @Test
    fun `a deleted holding is gone`() = runTest {
        repository.saveHolding(createHolding(COIN_ID, "0.5"))

        val deleteResult = repository.deleteHolding(COIN_ID)

        assertThat(deleteResult).isEqualTo(Resource.Success(Unit))
        assertThat(
            repository.getHoldings().first(),
        ).isEqualTo(Resource.Success(emptyList<Holding>()))
    }

    @Test
    fun `the holdings flow emits again after a save`() = runTest {
        repository.getHoldings().test {
            assertThat(awaitItem()).isEqualTo(Resource.Success(emptyList<Holding>()))

            repository.saveHolding(createHolding(COIN_ID, "0.5"))

            assertThat(awaitItem()).isEqualTo(
                Resource.Success(
                    listOf(Holding(COIN_ID, BigDecimal("0.5"))),
                ),
            )
        }
    }

    // Closing the database makes the query fail. Room throws IllegalStateException,
    // so it maps to Unexpected.
    @Test
    fun `a read from a closed database becomes a failure`() = runTest {
        databaseRule.database.close()

        assertThat(
            repository.getHolding(COIN_ID).first(),
        ).isEqualTo(Resource.Failure(AppError.Unexpected))
    }

    @Test
    fun `a write to a closed database becomes a failure`() = runTest {
        databaseRule.database.close()

        assertThat(repository.saveHolding(createHolding(COIN_ID, "0.5"))).isEqualTo(
            Resource.Failure(
                AppError.Unexpected,
            ),
        )
    }
}
