package me.prokhorov.coinfolio.data.testing

import androidx.room.Room
import me.prokhorov.coinfolio.data.local.CoinFolioDatabase
import me.prokhorov.coinfolio.data.local.dao.HoldingDao
import org.junit.rules.ExternalResource
import org.robolectric.RuntimeEnvironment

/**
 * Builds a real Room database that lives in memory only. Every test then starts on an empty
 * table, and nothing is left on disk.
 *
 * Robolectric supplies the application context. So a test using this rule must run with
 * `RobolectricTestRunner`.
 */
class InMemoryDatabaseRule : ExternalResource() {

    lateinit var database: CoinFolioDatabase
        private set

    val holdingDao: HoldingDao get() = database.holdingDao()

    override fun before() {
        database = Room
            .inMemoryDatabaseBuilder(
                RuntimeEnvironment.getApplication(),
                CoinFolioDatabase::class.java,
            )
            .build()
    }

    // Closing twice is safe. So a test can close the database itself to test this failure path.
    override fun after() {
        database.close()
    }
}
