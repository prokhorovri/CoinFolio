package me.prokhorov.coinfolio.navigation

/** The app navigation that the window shows. */
enum class NavigationType { BAR, RAIL, NONE }

/** A screen with no tab, like the coin detail, shows no navigation. */
fun navigationType(isTopLevel: Boolean, isWideWindow: Boolean): NavigationType {
    return when {
        !isTopLevel -> NavigationType.NONE
        isWideWindow -> NavigationType.RAIL
        else -> NavigationType.BAR
    }
}
