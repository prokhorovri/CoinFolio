package me.prokhorov.coinfolio.data.mapper

import me.prokhorov.coinfolio.data.remote.dto.CoinDetailDto
import me.prokhorov.coinfolio.domain.model.CoinDetail

fun CoinDetailDto.toDomain(): CoinDetail {
    return CoinDetail(
        id = id,
        symbol = symbol,
        name = name,
        imageUrl = image?.large.orEmpty(),
        currentPrice = marketData?.currentPrice?.usd,
        priceChangePercent24h = marketData?.priceChangePercentage24h,
        marketCapRank = marketCapRank,
        high24h = marketData?.high24h?.usd,
        low24h = marketData?.low24h?.usd,
        marketCap = marketData?.marketCap?.usd,
        totalVolume = marketData?.totalVolume?.usd,
        allTimeHigh = marketData?.ath?.usd,
        circulatingSupply = marketData?.circulatingSupply,
    )
}
