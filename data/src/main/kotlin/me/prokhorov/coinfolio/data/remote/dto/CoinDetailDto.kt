package me.prokhorov.coinfolio.data.remote.dto

import java.math.BigDecimal
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.prokhorov.coinfolio.data.remote.serializer.BigDecimalSerializer

@Serializable
data class CoinDetailDto(
    val id: String,
    val symbol: String,
    val name: String,
    @SerialName("market_cap_rank")
    val marketCapRank: Int? = null,
    val image: CoinImageDto? = null,
    @SerialName("market_data")
    val marketData: CoinMarketDataDto? = null,
)

@Serializable
data class CoinImageDto(
    val large: String? = null,
)

@Serializable
data class CoinMarketDataDto(
    @SerialName("current_price")
    val currentPrice: CurrencyAmountDto? = null,
    @SerialName("price_change_percentage_24h")
    @Serializable(with = BigDecimalSerializer::class)
    val priceChangePercentage24h: BigDecimal? = null,
    @SerialName("high_24h")
    val high24h: CurrencyAmountDto? = null,
    @SerialName("low_24h")
    val low24h: CurrencyAmountDto? = null,
    @SerialName("market_cap")
    val marketCap: CurrencyAmountDto? = null,
    @SerialName("total_volume")
    val totalVolume: CurrencyAmountDto? = null,
    val ath: CurrencyAmountDto? = null,
    @SerialName("circulating_supply")
    @Serializable(with = BigDecimalSerializer::class)
    val circulatingSupply: BigDecimal? = null,
)

/**
 * `coins/{id}` ignores `vs_currency`. It returns every money field for all currencies, with
 * the currency code as the key. Only `usd` is declared here, so the other ~60 keys are not
 * parsed.
 */
@Serializable
data class CurrencyAmountDto(
    @Serializable(with = BigDecimalSerializer::class)
    val usd: BigDecimal? = null,
)
