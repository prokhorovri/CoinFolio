package me.prokhorov.coinfolio.data.local.dao

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity
import me.prokhorov.coinfolio.data.testing.InMemoryDatabaseRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val COIN_ID = "bitcoin"

/** Runs against a real SQLite, so these tests also cover the schema and the type converter. */
@RunWith(RobolectricTestRunner::class)
class HoldingDaoTest {

    @get:Rule
    val databaseRule = InMemoryDatabaseRule()

    private val dao: HoldingDao get() = databaseRule.holdingDao

    @Test
    fun `a saved holding is read back`() = runTest {
        val entity = HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("0.5"))

        dao.upsertHolding(entity)

        assertThat(dao.getHolding(COIN_ID).first()).isEqualTo(entity)
    }

    @Test
    fun `an unknown coin reads as null`() = runTest {
        assertThat(dao.getHolding(COIN_ID).first()).isNull()
    }

    @Test
    fun `upsert replaces the holding of the same coin`() = runTest {
        dao.upsertHolding(HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("0.5")))

        dao.upsertHolding(HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("1.5")))

        assertThat(dao.getHoldings().first()).containsExactly(
            HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("1.5")),
        )
    }

    @Test
    fun `two coins are kept side by side`() = runTest {
        dao.upsertHolding(HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("0.5")))

        dao.upsertHolding(HoldingEntity(coinId = "ethereum", quantity = BigDecimal("2")))

        assertThat(dao.getHoldings().first()).hasSize(2)
    }

    @Test
    fun `a deleted holding is gone`() = runTest {
        dao.upsertHolding(HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("0.5")))

        dao.deleteHolding(COIN_ID)

        assertThat(dao.getHoldings().first()).isEmpty()
    }

    @Test
    fun `deleting an unknown coin changes nothing`() = runTest {
        dao.upsertHolding(HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("0.5")))

        dao.deleteHolding("ethereum")

        assertThat(dao.getHoldings().first()).hasSize(1)
    }

    @Test
    fun `the holdings flow emits again after a write`() = runTest {
        dao.getHoldings().test {
            assertThat(awaitItem()).isEmpty()

            dao.upsertHolding(HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("0.5")))

            assertThat(awaitItem()).hasSize(1)
        }
    }

    @Test
    fun `the single holding flow emits again after a write`() = runTest {
        dao.getHolding(COIN_ID).test {
            assertThat(awaitItem()).isNull()

            dao.upsertHolding(HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("0.5")))

            assertThat(awaitItem()?.quantity).isEqualTo(BigDecimal("0.5"))
        }
    }

    // SHIB amounts can have many decimal places. We store them as TEXT to avoid losing digits,
    // so this checks that the value keeps its full precision.
    @Test
    fun `a shib sized quantity keeps every digit`() = runTest {
        val quantity = BigDecimal("123456789012.12345678")

        dao.upsertHolding(HoldingEntity(coinId = "shiba-inu", quantity = quantity))

        assertThat(dao.getHolding("shiba-inu").first()?.quantity).isEqualTo(quantity)
    }

    // The user can type 0.50, and the screen must show 0.50 again after a restart.
    @Test
    fun `a trailing zero survives the round trip`() = runTest {
        dao.upsertHolding(HoldingEntity(coinId = COIN_ID, quantity = BigDecimal("0.50")))

        assertThat(dao.getHolding(COIN_ID).first()?.quantity).isEqualTo(BigDecimal("0.50"))
    }
}
