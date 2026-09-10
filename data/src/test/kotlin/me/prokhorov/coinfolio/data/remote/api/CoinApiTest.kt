package me.prokhorov.coinfolio.data.remote.api

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.data.testing.COIN_DETAIL_RESPONSE
import me.prokhorov.coinfolio.data.testing.MARKETS_RESPONSE
import me.prokhorov.coinfolio.data.testing.createCoinApi
import me.prokhorov.coinfolio.data.testing.createJsonResponse
import mockwebserver3.junit4.MockWebServerRule
import org.junit.Rule
import org.junit.Test

private const val EMPTY_LIST_BODY = "[]"
private const val MINIMAL_COIN_BODY = """{"id":"bitcoin","symbol":"btc","name":"Bitcoin"}"""

/**
 * Checks the request that Retrofit builds from the annotations on [CoinApi]. It also checks how
 * a real response body is parsed. A wrong query parameter gives no error at runtime. CoinGecko
 * answers with its own default. The screen then shows data that looks right, but is not what
 * we asked for.
 */
class CoinApiTest {

    @get:Rule
    val serverRule = MockWebServerRule()

    private val server get() = serverRule.server

    @Test
    fun `the top coins request asks for the markets path`() = runTest {
        server.enqueue(createJsonResponse(EMPTY_LIST_BODY))

        server.createCoinApi().getTopCoins()

        assertThat(server.takeRequest().url.encodedPath).isEqualTo("/coins/markets")
    }

    @Test
    fun `the top coins request asks for the first 50 by market cap`() = runTest {
        server.enqueue(createJsonResponse(EMPTY_LIST_BODY))

        server.createCoinApi().getTopCoins()

        val url = server.takeRequest().url
        assertThat(url.queryParameter("vs_currency")).isEqualTo("usd")
        assertThat(url.queryParameter("order")).isEqualTo("market_cap_desc")
        assertThat(url.queryParameter("per_page")).isEqualTo("50")
        assertThat(url.queryParameter("page")).isEqualTo("1")
        assertThat(url.queryParameter("sparkline")).isEqualTo("true")
    }

    @Test
    fun `the coins by ids request puts every id in one query value`() = runTest {
        server.enqueue(createJsonResponse(EMPTY_LIST_BODY))

        server.createCoinApi().getCoinsByIds(ids = "bitcoin,ethereum")

        val url = server.takeRequest().url
        assertThat(url.encodedPath).isEqualTo("/coins/markets")
        assertThat(url.queryParameter("ids")).isEqualTo("bitcoin,ethereum")
    }

    // The chart of the portfolio rows does not exist, so the sparkline data would be parsed
    // and dropped. The top coins request asks for it on purpose, this one must not.
    @Test
    fun `the coins by ids request asks for no sparkline`() = runTest {
        server.enqueue(createJsonResponse(EMPTY_LIST_BODY))

        server.createCoinApi().getCoinsByIds(ids = "bitcoin")

        val url = server.takeRequest().url
        assertThat(url.queryParameter("vs_currency")).isEqualTo("usd")
        assertThat(url.queryParameter("sparkline")).isEqualTo("false")
    }

    @Test
    fun `a markets response is read into the coin list`() = runTest {
        server.enqueue(createJsonResponse(MARKETS_RESPONSE))

        val coins = server.createCoinApi().getTopCoins()

        assertThat(coins).hasSize(1)
        assertThat(coins.first().id).isEqualTo("bitcoin")
        assertThat(coins.first().imageUrl).isEqualTo("https://example.com/bitcoin.png")
        assertThat(coins.first().currentPrice).isEqualTo(BigDecimal("67000.55"))
    }

    @Test
    fun `the detail request puts the coin id in the path`() = runTest {
        server.enqueue(createJsonResponse(MINIMAL_COIN_BODY))

        server.createCoinApi().getCoinDetail(id = "wrapped-bitcoin")

        assertThat(server.takeRequest().url.encodedPath).isEqualTo("/coins/wrapped-bitcoin")
    }

    // Without these flags the answer also carries every exchange ticker and the community stats.
    // That is a few hundred KB we would parse and drop.
    @Test
    fun `the detail request skips the data the screen does not show`() = runTest {
        server.enqueue(createJsonResponse(MINIMAL_COIN_BODY))

        server.createCoinApi().getCoinDetail(id = "bitcoin")

        val url = server.takeRequest().url
        assertThat(url.queryParameter("market_data")).isEqualTo("true")
        assertThat(url.queryParameter("localization")).isEqualTo("false")
        assertThat(url.queryParameter("tickers")).isEqualTo("false")
        assertThat(url.queryParameter("community_data")).isEqualTo("false")
        assertThat(url.queryParameter("developer_data")).isEqualTo("false")
        assertThat(url.queryParameter("sparkline")).isEqualTo("false")
    }

    @Test
    fun `a detail response is read into the nested market data`() = runTest {
        server.enqueue(createJsonResponse(COIN_DETAIL_RESPONSE))

        val detail = server.createCoinApi().getCoinDetail(id = "bitcoin")

        assertThat(detail.marketCapRank).isEqualTo(1)
        assertThat(detail.image?.large).isEqualTo("https://example.com/bitcoin.png")
        assertThat(detail.marketData?.currentPrice?.usd).isEqualTo(BigDecimal("67000.55"))
        assertThat(detail.marketData?.circulatingSupply).isEqualTo(BigDecimal("19700000.0"))
    }
}
