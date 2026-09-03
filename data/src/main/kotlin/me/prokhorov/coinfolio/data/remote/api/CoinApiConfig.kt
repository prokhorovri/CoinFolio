package me.prokhorov.coinfolio.data.remote.api

import okhttp3.MediaType.Companion.toMediaType

object CoinApiConfig {
    const val BASE_URL = "https://api.coingecko.com/api/v3/"
    val JSON_MEDIA_TYPE = "application/json".toMediaType()
}
