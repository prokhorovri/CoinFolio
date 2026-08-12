package me.prokhorov.coinfolio.domain.model

import java.math.BigDecimal

/**
 * The model for one coin. It is separate from [Coin] because the endpoint used for the list
 * cannot provide these fields. One shared model would make every extra field nullable for list
 * items too. A nullable field here means the API really does not return it: new coins have no
 * all-time high, and some coins report no supply.
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
