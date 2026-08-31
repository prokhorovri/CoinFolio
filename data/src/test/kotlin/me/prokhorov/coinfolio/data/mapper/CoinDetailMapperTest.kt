package me.prokhorov.coinfolio.data.mapper

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import me.prokhorov.coinfolio.data.remote.dto.CoinDetailDto
import me.prokhorov.coinfolio.data.remote.dto.CoinImageDto
import me.prokhorov.coinfolio.data.remote.dto.CoinMarketDataDto
import me.prokhorov.coinfolio.data.remote.dto.CurrencyAmountDto
import me.prokhorov.coinfolio.domain.model.CoinDetail
import org.junit.Test

class CoinDetailMapperTest {

    @Test
    fun `every field goes to the domain model`() {
        val dto = CoinDetailDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            marketCapRank = 1,
            image = CoinImageDto(large = "https://example.com/bitcoin.png"),
            marketData = CoinMarketDataDto(
                currentPrice = CurrencyAmountDto(BigDecimal("67000.55")),
                priceChangePercentage24h = BigDecimal("-1.25"),
                high24h = CurrencyAmountDto(BigDecimal("68000")),
                low24h = CurrencyAmountDto(BigDecimal("66000")),
                marketCap = CurrencyAmountDto(BigDecimal("1300000000000")),
                totalVolume = CurrencyAmountDto(BigDecimal("25000000000")),
                ath = CurrencyAmountDto(BigDecimal("73000")),
                circulatingSupply = BigDecimal("19700000"),
            ),
        )

        val detail = dto.toDomain()

        assertThat(detail).isEqualTo(
            CoinDetail(
                id = "bitcoin",
                symbol = "btc",
                name = "Bitcoin",
                imageUrl = "https://example.com/bitcoin.png",
                currentPrice = BigDecimal("67000.55"),
                priceChangePercent24h = BigDecimal("-1.25"),
                marketCapRank = 1,
                high24h = BigDecimal("68000"),
                low24h = BigDecimal("66000"),
                marketCap = BigDecimal("1300000000000"),
                totalVolume = BigDecimal("25000000000"),
                allTimeHigh = BigDecimal("73000"),
                circulatingSupply = BigDecimal("19700000"),
            ),
        )
    }

    // The whole market_data block is optional in the response, and the mapper must not crash on
    // it. Every money field then stays null, and the screen shows a dash for each one.
    @Test
    fun `a response with no market data leaves the money fields null`() {
        val dto = CoinDetailDto(id = "bitcoin", symbol = "btc", name = "Bitcoin")

        val detail = dto.toDomain()

        assertThat(detail.currentPrice).isNull()
        assertThat(detail.priceChangePercent24h).isNull()
        assertThat(detail.marketCapRank).isNull()
        assertThat(detail.high24h).isNull()
        assertThat(detail.low24h).isNull()
        assertThat(detail.marketCap).isNull()
        assertThat(detail.totalVolume).isNull()
        assertThat(detail.allTimeHigh).isNull()
        assertThat(detail.circulatingSupply).isNull()
    }

    // The image url is not nullable in the domain model, because Coil takes a String. An empty
    // one means "no image", and Coil shows the placeholder.
    @Test
    fun `a missing image gives an empty url`() {
        val dto = CoinDetailDto(id = "bitcoin", symbol = "btc", name = "Bitcoin", image = null)

        val detail = dto.toDomain()

        assertThat(detail.imageUrl).isEmpty()
    }

    @Test
    fun `an image with no large size gives an empty url`() {
        val dto = CoinDetailDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            image = CoinImageDto(large = null),
        )

        val detail = dto.toDomain()

        assertThat(detail.imageUrl).isEmpty()
    }

    // Only the usd amount is read. A missing usd key means no value, not zero.
    @Test
    fun `a currency block with no usd amount stays null`() {
        val dto = CoinDetailDto(
            id = "bitcoin",
            symbol = "btc",
            name = "Bitcoin",
            marketData = CoinMarketDataDto(currentPrice = CurrencyAmountDto(usd = null)),
        )

        val detail = dto.toDomain()

        assertThat(detail.currentPrice).isNull()
    }
}
