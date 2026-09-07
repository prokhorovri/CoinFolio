package me.prokhorov.coinfolio.domain.model

import java.math.BigDecimal

/**
 * The model for one coin. It is separate from [Coin] because the list endpoint does not
 * return these fields. A nullable field means the API really does not send it: new coins
 * have no all-time high, and some coins report no supply.
 */
data class CoinDetail(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val currentPrice: BigDecimal?,
    val priceChangePercent24h: BigDecimal?,
    val marketCapRank: Int?,
    val high24h: BigDecimal?,
    val low24h: BigDecimal?,
    val marketCap: BigDecimal?,
    val totalVolume: BigDecimal?,
    val allTimeHigh: BigDecimal?,
    val circulatingSupply: BigDecimal?,
)
