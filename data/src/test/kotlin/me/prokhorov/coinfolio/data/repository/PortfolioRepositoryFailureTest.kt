package me.prokhorov.coinfolio.data.repository

import android.database.SQLException
import com.google.common.truth.Truth.assertThat
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.data.local.HoldingLocalDataSource
import me.prokhorov.coinfolio.data.local.dao.HoldingDao
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.createHolding
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val COIN_ID = "bitcoin"

/**
 * A real in-memory database cannot run out of disk space, so the `AppError.Storage`
 * path needs a DAO that fails on purpose.
 *
 * Robolectric is needed here because a plain unit test cannot create an `SQLException`
 * with the stub `android.jar`.
 */
@RunWith(RobolectricTestRunner::class)
class PortfolioRepositoryFailureTest {

    private val storageError: suspend () -> Nothing = {
        throw SQLException("no space left on device")
    }

    private fun createRepository(fail: suspend () -> Nothing): PortfolioRepositoryImpl {
        return PortfolioRepositoryImpl(HoldingLocalDataSource(FailingHoldingDao(fail)))
    }

    @Test
    fun `a write that hits a storage error becomes a storage failure`() = runTest {
        val result = createRepository(storageError).saveHolding(createHolding(COIN_ID, "0.5"))

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }

    @Test
    fun `a delete that hits a storage error becomes a storage failure`() = runTest {
        val result = createRepository(storageError).deleteHolding(COIN_ID)

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }

    @Test
    fun `a read that hits a storage error becomes a storage failure`() = runTest {
        val result = createRepository(storageError).getHolding(COIN_ID).first()

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }

    @Test
    fun `the holdings read reports a storage error as a value`() = runTest {
        val result = createRepository(storageError).getHoldings().first()

        assertThat(result).isEqualTo(Resource.Failure(AppError.Storage))
    }

    @Test
    fun `an unknown error becomes an unexpected failure`() = runTest {
        val result = createRepository { throw IllegalStateException("a bug on our side") }
            .saveHolding(createHolding(COIN_ID, "0.5"))

        assertThat(result).isEqualTo(Resource.Failure(AppError.Unexpected))
    }

    // Leaving the screen cancels the coroutine. This is not a failure,
    // so it should stay as a cancellation instead of becoming Resource.Failure.
    @Test
    fun `a cancelled write is not turned into a failure`() = runTest {
        val repository = createRepository {
            currentCoroutineContext().cancel()
            throw CancellationException("the user left the screen")
        }

        val thrown = runCatching {
            coroutineScope { repository.saveHolding(createHolding(COIN_ID, "0.5")) }
        }.exceptionOrNull()

        assertThat(thrown).isInstanceOf(CancellationException::class.java)
    }
}

private class FailingHoldingDao(private val fail: suspend () -> Nothing) : HoldingDao {

    override fun getHoldings(): Flow<List<HoldingEntity>> = flow { fail() }

    override fun getHolding(coinId: String): Flow<HoldingEntity?> = flow { fail() }

    override suspend fun deleteHolding(coinId: String) = fail()

    override suspend fun upsertHolding(holding: HoldingEntity) = fail()
}
