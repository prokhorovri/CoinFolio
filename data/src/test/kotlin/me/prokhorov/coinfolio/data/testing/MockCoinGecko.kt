package me.prokhorov.coinfolio.data.testing

import java.util.concurrent.TimeUnit
import kotlinx.serialization.json.Json
import me.prokhorov.coinfolio.data.remote.api.CoinApi
import me.prokhorov.coinfolio.data.remote.api.CoinApiConfig
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val CONTENT_TYPE_HEADER = "Content-Type"

/** The OkHttp default. A test that wants a timeout passes a shorter one. */
private const val DEFAULT_READ_TIMEOUT_MILLIS = 10_000L

/** The same Json settings as NetworkModule in :app. A test then reads what the app reads. */
private val json = Json { ignoreUnknownKeys = true }

/**
 * Builds a [CoinApi] that calls this test server. Retrofit, OkHttp and the JSON converter are
 * the same ones the app uses. So a test checks the request and the parsing that really happen.
 * The base URL is the server address, not [CoinApiConfig.BASE_URL]. A recorded path is then
 * only the part that [CoinApi] adds, like `/coins/markets`.
 */
fun MockWebServer.createCoinApi(
    readTimeoutMillis: Long = DEFAULT_READ_TIMEOUT_MILLIS,
): CoinApi {
    val client = OkHttpClient.Builder()
        .readTimeout(readTimeoutMillis, TimeUnit.MILLISECONDS)
        .build()

    return Retrofit.Builder()
        .baseUrl(url("/"))
        .client(client)
        .addConverterFactory(json.asConverterFactory(CoinApiConfig.JSON_MEDIA_TYPE))
        .build()
        .create(CoinApi::class.java)
}

fun createJsonResponse(body: String): MockResponse {
    return MockResponse.Builder()
        .addHeader(CONTENT_TYPE_HEADER, CoinApiConfig.JSON_MEDIA_TYPE)
        .body(body)
        .build()
}

/** An error status with an empty body. Retrofit throws an HttpException for any error status. */
fun createErrorResponse(code: Int): MockResponse = MockResponse.Builder().code(code).build()

/** One coin from `coins/markets`. It has every field that CoinDto declares. */
const val MARKETS_RESPONSE = """
    [
      {
        "id": "bitcoin",
        "symbol": "btc",
        "name": "Bitcoin",
        "image": "https://example.com/bitcoin.png",
        "current_price": 67000.55,
        "price_change_percentage_24h": -1.25
      }
    ]
"""

/**
 * One coin from `coins/{id}`. `current_price` holds a second currency on purpose. The endpoint
 * sends every currency it knows, and we read only usd.
 */
const val COIN_DETAIL_RESPONSE = """
    {
      "id": "bitcoin",
      "symbol": "btc",
      "name": "Bitcoin",
      "market_cap_rank": 1,
      "image": { "large": "https://example.com/bitcoin.png" },
      "market_data": {
        "current_price": { "usd": 67000.55, "eur": 61000.10 },
        "price_change_percentage_24h": -1.25,
        "high_24h": { "usd": 68000.00 },
        "low_24h": { "usd": 66000.00 },
        "market_cap": { "usd": 1320000000000 },
        "total_volume": { "usd": 25000000000 },
        "ath": { "usd": 73750.07 },
        "circulating_supply": 19700000.0
      }
    }
"""
