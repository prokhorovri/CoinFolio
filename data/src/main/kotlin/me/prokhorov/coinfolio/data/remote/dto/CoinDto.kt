package me.prokhorov.coinfolio.data.remote.dto

import java.math.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.prokhorov.coinfolio.data.remote.serializer.BigDecimalSerializer

@Serializable
data class CoinDto(
    val id: String,
    val symbol: String,
    val name: String,
    @SerialName("image")
    val imageUrl: String,
    @SerialName("current_price")
    @Serializable(with = BigDecimalSerializer::class)
    val currentPrice: BigDecimal?,
    @SerialName("price_change_percentage_24h")
    @Serializable(with = BigDecimalSerializer::class)
    val priceChangePercentage24h: BigDecimal?,
)
