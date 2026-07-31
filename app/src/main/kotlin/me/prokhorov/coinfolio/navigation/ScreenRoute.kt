package me.prokhorov.coinfolio.navigation

import kotlinx.serialization.Serializable

sealed interface ScreenRoute {
    @Serializable
    data object CoinList : ScreenRoute

    @Serializable
    data class CoinDetail(val id: String) : ScreenRoute
}
