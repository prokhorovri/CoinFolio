package me.prokhorov.coinfolio.data.remote.dto

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.serialization.json.Json
import org.junit.Test

/** The same settings as the Json in NetworkModule, so the tests read what the app reads. */
private val json = Json { ignoreUnknownKeys = true }

private const val FULL_COIN = """
    {
      "id": "bitcoin",
      "symbol": "btc",
      "name": "Bitcoin",
      "image": "https://example.com/bitcoin.png",
      "current_price": 67000.55,
      "price_change_percentage_24h": -1.25
    }
"""

class CoinDtoTest {

    @Test
    fun `a full coin is read into every field`() {
        val dto = json.decodeFromString<CoinDto>(FULL_COIN)

        assertThat(dto.id).isEqualTo("bitcoin")
        assertThat(dto.symbol).isEqualTo("btc")
        assertThat(dto.name).isEqualTo("Bitcoin")
        assertThat(dto.imageUrl).isEqualTo("https://example.com/bitcoin.png")
        assertThat(dto.currentPrice).isEqualTo(BigDecimal("67000.55"))
        assertThat(dto.priceChangePercentage24h).isEqualTo(BigDecimal("-1.25"))
    }

    @Test
    fun `fields the API does not know are skipped`() {
        val withExtra = """{"id":"btc","symbol":"btc","name":"Bitcoin","image":"u",
            "current_price":1,"price_change_percentage_24h":1,"sparkline_in_7d":{"price":[1,2]}}"""

        val dto = json.decodeFromString<CoinDto>(withExtra)

        assertThat(dto.id).isEqualTo("btc")
    }

    // CoinGecko sends null for some small or delisted coins. The model allows it, and the screen
    // shows a dash.
    @Test
    fun `a null price is read as null`() {
        val nullPrice = """{"id":"a","symbol":"a","name":"A","image":"u",
            "current_price":null,"price_change_percentage_24h":null}"""

        val dto = json.decodeFromString<CoinDto>(nullPrice)

        assertThat(dto.currentPrice).isNull()
        assertThat(dto.priceChangePercentage24h).isNull()
    }

    // Some coins don't have this value, but that shouldn't break the whole list.
    @Test
    fun `a coin with no price field is read as null`() {
        val noPrice = """{"id":"a","symbol":"a","name":"A","image":"u"}"""

        val dto = json.decodeFromString<CoinDto>(noPrice)

        assertThat(dto.currentPrice).isNull()
        assertThat(dto.priceChangePercentage24h).isNull()
    }
}
