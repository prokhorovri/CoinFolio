package me.prokhorov.coinfolio.domain.model

import java.math.BigDecimal

data class Coin(
    val id: String,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val currentPrice: BigDecimal,
    val priceChangePercent24h: BigDecimal,
)
