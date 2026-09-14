package me.prokhorov.coinfolio.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import me.prokhorov.coinfolio.R

/**
 * The screens of the bottom bar. The order here is the order on screen.
 */
enum class TopLevelDestination(
    val route: ScreenRoute,
    @get:DrawableRes val iconRes: Int,
    @get:StringRes val labelRes: Int,
) {
    COINS(
        route = ScreenRoute.CoinList,
        iconRes = R.drawable.ic_coins,
        labelRes = R.string.navigation_bar_coins_text,
    ),
    PORTFOLIO(
        route = ScreenRoute.Portfolio,
        iconRes = R.drawable.ic_portfolio,
        labelRes = R.string.navigation_bar_portfolio_text,
    ),
}
