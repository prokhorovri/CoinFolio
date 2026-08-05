package me.prokhorov.coinfolio.domain.model

import java.math.BigDecimal

/**
 * The single-coin view, kept separate from [Coin] because the markets endpoint behind the list
 * cannot fill these fields. Merging the two would make every extra field nullable for list items.
 * Nullable here means the API genuinely omits it: young coins have no all-time high, and some
 * have no reported supply.
 */
data class CoinDetail(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val currentPrice: BigDecimal,
    val priceChangePercent24h: BigDecimal,
    val marketCapRank: Int?,
    val high24h: BigDecimal?,
    val low24h: BigDecimal?,
    val marketCap: BigDecimal?,
    val totalVolume: BigDecimal?,
    val allTimeHigh: BigDecimal?,
    val circulatingSupply: BigDecimal?,
)
