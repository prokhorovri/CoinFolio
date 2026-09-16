package me.prokhorov.coinfolio.domain.testing

import java.math.BigDecimal
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.model.Holding

/** Short builders, so a test sets only the field it cares about. */

fun createCoin(id: String, symbol: String = id.take(3), price: String = "100"): Coin = Coin(
    id = id,
    symbol = symbol,
    name = id,
    imageUrl = "https://example.com/$id.png",
    currentPrice = BigDecimal(price),
    priceChangePercent24h = BigDecimal("1.5"),
)

fun createCoinDetail(
    id: String,
    symbol: String = id.take(3),
    price: String = "100",
): CoinDetail = CoinDetail(
    id = id,
    symbol = symbol,
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

fun createHolding(coinId: String, quantity: String): Holding = Holding(
    coinId,
    BigDecimal(quantity),
)
