package me.prokhorov.coinfolio.data.repository

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.data.remote.CoinRemoteDataSource
import me.prokhorov.coinfolio.data.remote.api.CoinApi
import me.prokhorov.coinfolio.data.testing.MARKETS_RESPONSE
import me.prokhorov.coinfolio.data.testing.createCoinApi
import me.prokhorov.coinfolio.data.testing.createErrorResponse
import me.prokhorov.coinfolio.data.testing.createJsonResponse
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import mockwebserver3.MockResponse
import mockwebserver3.junit4.MockWebServerRule
import org.junit.Rule
import org.junit.Test

private const val COIN_ID = "bitcoin"
private const val RATE_LIMITED_CODE = 429
private const val NOT_FOUND_CODE = 404
private const val SERVER_ERROR_CODE = 500

private const val SHORT_READ_TIMEOUT_MILLIS = 100L
private const val LATE_HEADERS_SECONDS = 1L

private val bitcoin = Coin(
    id = "bitcoin",
    symbol = "btc",
    name = "Bitcoin",
    imageUrl = "https://example.com/bitcoin.png",
    currentPrice = BigDecimal("67000.55"),
    priceChangePercent24h = BigDecimal("-1.25"),
)

/**
 * The whole remote stack: Retrofit, the JSON converter, the mappers and `apiCall`.
 *
 * It shows what a ViewModel gets for each kind of answer. `apiCall` never lets an exception out,
 * so every failure arrives as a [Resource].
 */
class CoinRepositoryImplTest {

    @get:Rule
    val serverRule = MockWebServerRule()

    private val server get() = serverRule.server

    private fun createRepository(api: CoinApi = server.createCoinApi()): CoinRepositoryImpl {
        return CoinRepositoryImpl(CoinRemoteDataSource(api))
    }

    @Test
    fun `a markets response becomes a list of coins`() = runTest {
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))

        val result = createRepository().getTopCoins()

        assertThat(result).isEqualTo(Resource.Success(listOf(bitcoin)))
    }

    // A failed call also uses up the rate limit. So this case needs its own message on screen.
    @Test
    fun `a 429 becomes a rate limited failure`() = runTest {
        server.enqueue(createErrorResponse(RATE_LIMITED_CODE))

        val result = createRepository().getTopCoins()

        assertThat(result).isEqualTo(Resource.Failure(AppError.RateLimited))
    }

    @Test
    fun `a 404 becomes a not found failure`() = runTest {
        server.enqueue(createErrorResponse(NOT_FOUND_CODE))

        val result = createRepository().getCoinDetail(COIN_ID)

        assertThat(result).isEqualTo(Resource.Failure(AppError.NotFound))
    }

    @Test
    fun `any other status keeps its code`() = runTest {
        server.enqueue(createErrorResponse(SERVER_ERROR_CODE))

        val result = createRepository().getTopCoins()

        assertThat(result).isEqualTo(Resource.Failure(AppError.Server(SERVER_ERROR_CODE)))
    }

    // A 200 answer with a body that is not the JSON we declared. No branch handles that case,
    // so it stays unexpected.
    @Test
    fun `a body that is not the JSON we expect becomes an unexpected failure`() = runTest {
        server.enqueue(createJsonResponse("<html>maintenance</html>"))

        val result = createRepository().getTopCoins()

        assertThat(result).isEqualTo(Resource.Failure(AppError.Unexpected))
    }

    @Test
    fun `a response that arrives too late becomes a network failure`() = runTest {
        server.enqueue(
            MockResponse.Builder()
                .headersDelay(LATE_HEADERS_SECONDS, TimeUnit.SECONDS)
                .body(MARKETS_RESPONSE)
                .build(),
        )
        val slowApi = server.createCoinApi(readTimeoutMillis = SHORT_READ_TIMEOUT_MILLIS)

        val result = createRepository(slowApi).getTopCoins()

        assertThat(result).isEqualTo(Resource.Failure(AppError.Network))
    }
}
