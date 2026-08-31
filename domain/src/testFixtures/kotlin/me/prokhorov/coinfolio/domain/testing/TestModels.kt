package me.prokhorov.coinfolio.domain.testing

import java.math.BigDecimal
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.model.Holding

/** Short builders, so a test says only the field it cares about. */

fun coin(id: String, price: String = "100"): Coin {
    return Coin(
        id = id,
        symbol = id.take(3),
        name = id,
        imageUrl = "https://example.com/$id.png",
        currentPrice = BigDecimal(price),
        priceChangePercent24h = BigDecimal("1.5"),
    )
}

fun coinDetail(id: String, price: String = "100"): CoinDetail {
    return CoinDetail(
        id = id,
        symbol = id.take(3),
        name = id,
        imageUrl = "https://example.com/$id.png",
        currentPrice = BigDecimal(price),
        priceChangePercent24h = BigDecimal("1.5"),
        marketCapRank = 1,
        high24h = null,
        low24h = null,
        marketCap = null,
        totalVolume = null,
        allTimeHigh = null,
        circulatingSupply = null,
    )
}

fun holding(coinId: String, quantity: String): Holding = Holding(coinId, BigDecimal(quantity))
