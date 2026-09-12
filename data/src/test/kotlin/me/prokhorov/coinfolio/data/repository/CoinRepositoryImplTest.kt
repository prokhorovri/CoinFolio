package me.prokhorov.coinfolio.data.repository

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.data.remote.CoinRemoteDataSource
import me.prokhorov.coinfolio.data.remote.MarketCoinsCache
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

private val CACHE_VALID_FOR = 30.seconds

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
 * The whole remote stack: Retrofit, the JSON converter, the mappers and `apiCall`. It shows what
 * a ViewModel gets for each kind of answer. `apiCall` catches every exception, so a failure
 * always arrives as a [Resource].
 */
class CoinRepositoryImplTest {

    @get:Rule
    val serverRule = MockWebServerRule()

    private val server get() = serverRule.server

    private val timeSource = TestTimeSource()

    // Each test builds its own repository, so each one starts with an empty cache.
    private fun createRepository(api: CoinApi = server.createCoinApi()): CoinRepositoryImpl {
        return CoinRepositoryImpl(CoinRemoteDataSource(api), MarketCoinsCache(timeSource))
    }

    @Test
    fun `a markets response becomes a list of coins`() = runTest {
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))

        val result = createRepository().getTopCoins()

        assertThat(result).isEqualTo(Resource.Success(listOf(bitcoin)))
    }

    // A failed call also uses up the rate limit. So this case needs its own message on screen.
    @Test
    fun `the coins of the given ids become a list of coins`() = runTest {
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))

        val result = createRepository().getCoinsByIds(listOf("bitcoin"))

        assertThat(result).isEqualTo(Resource.Success(listOf(bitcoin)))
    }

    // CoinGecko reads an empty `ids` as "no filter" and answers with the whole market. That
    // answer is useless here, and the request still counts toward the rate limit.
    @Test
    fun `an empty list of ids sends no request`() = runTest {
        val result = createRepository().getCoinsByIds(emptyList())

        assertThat(result).isEqualTo(Resource.Success(emptyList<Any>()))
        assertThat(server.requestCount).isEqualTo(0)
    }

    // The portfolio asks again after every change of a holding. Without the cache each save
    // would send a request, and CoinGecko allows about 5 to 15 requests per minute.
    @Test
    fun `the same ids twice send one request`() = runTest {
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))
        val repository = createRepository()

        val first = repository.getCoinsByIds(listOf("bitcoin"))
        val second = repository.getCoinsByIds(listOf("bitcoin"))

        assertThat(second).isEqualTo(first)
        assertThat(server.requestCount).isEqualTo(1)
    }

    @Test
    fun `other ids send a new request`() = runTest {
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))
        val repository = createRepository()

        repository.getCoinsByIds(listOf("bitcoin"))
        repository.getCoinsByIds(listOf("bitcoin", "ethereum"))

        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun `the same ids after 30 seconds send a new request`() = runTest {
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))
        val repository = createRepository()

        repository.getCoinsByIds(listOf("bitcoin"))
        timeSource += CACHE_VALID_FOR
        repository.getCoinsByIds(listOf("bitcoin"))

        assertThat(server.requestCount).isEqualTo(2)
    }

    @Test
    fun `a failed call is not stored`() = runTest {
        server.enqueue(createErrorResponse(RATE_LIMITED_CODE))
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))
        val repository = createRepository()

        val failure = repository.getCoinsByIds(listOf("bitcoin"))
        val second = repository.getCoinsByIds(listOf("bitcoin"))

        assertThat(failure).isEqualTo(Resource.Failure(AppError.RateLimited))
        assertThat(second).isEqualTo(Resource.Success(listOf(bitcoin)))
    }

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
