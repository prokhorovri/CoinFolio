package me.prokhorov.coinfolio.navigation

sealed class ScreenRoute(
    val route: String,
) {
    data object CoinList : ScreenRoute(route = "coin_list")
}
