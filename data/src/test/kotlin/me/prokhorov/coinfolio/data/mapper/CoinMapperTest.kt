package me.prokhorov.coinfolio.data.mapper

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import me.prokhorov.coinfolio.data.remote.dto.CoinDto
import me.prokhorov.coinfolio.domain.model.Coin
import org.junit.Test

class CoinMapperTest {

    @Test
    fun `every field goes to the domain model`() {
        val dto = CoinDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            imageUrl = "https://example.com/bitcoin.png",
            currentPrice = BigDecimal("67000.55"),
            priceChangePercentage24h = BigDecimal("-1.25"),
        )

        val coin = dto.toDomain()

        assertThat(coin).isEqualTo(
            Coin(
                id = "bitcoin",
                symbol = "btc",
                name = "Bitcoin",
                imageUrl = "https://example.com/bitcoin.png",
                currentPrice = BigDecimal("67000.55"),
                priceChangePercent24h = BigDecimal("-1.25"),
            ),
        )
    }

    // A missing price must stay missing. A zero here would look like a real price of 0 on screen.
    @Test
    fun `a coin with no price keeps null`() {
        val dto = CoinDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            imageUrl = "https://example.com/bitcoin.png",
            currentPrice = null,
            priceChangePercentage24h = null,
        )

        val coin = dto.toDomain()

        assertThat(coin.currentPrice).isNull()
        assertThat(coin.priceChangePercent24h).isNull()
    }

    // The price is money, so the digits must arrive as they were sent.
    @Test
    fun `the price keeps its scale`() {
        val dto = CoinDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            imageUrl = "u",
            currentPrice = BigDecimal("67000.50"),
            priceChangePercentage24h = null,
        )

        val coin = dto.toDomain()

        assertThat(coin.currentPrice).isEqualTo(BigDecimal("67000.50"))
    }
}
