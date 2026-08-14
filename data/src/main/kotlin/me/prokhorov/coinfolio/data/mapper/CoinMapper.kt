package me.prokhorov.coinfolio.data.mapper

import me.prokhorov.coinfolio.data.remote.dto.CoinDto
import me.prokhorov.coinfolio.domain.model.Coin

fun CoinDto.toDomain(): Coin {
    return Coin(
        id = id,
        symbol = symbol,
        name = name,
        imageUrl = imageUrl,
        currentPrice = currentPrice,
        priceChangePercent24h = priceChangePercentage24h,
    )
}
