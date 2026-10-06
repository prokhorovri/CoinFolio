package me.prokhorov.coinfolio.navigation

/** The app navigation that the window shows, with the selected tab. */
sealed interface NavigationType {
    data class Bar(val selected: TopLevelDestination) : NavigationType
    data class Rail(val selected: TopLevelDestination) : NavigationType
    data object None : NavigationType
}

/** A screen with no tab, like the coin detail, shows no navigation. */
fun navigationType(
    topLevelDestination: TopLevelDestination?,
    isWideWindow: Boolean,
): NavigationType {
    return when {
        topLevelDestination == null -> NavigationType.None
        isWideWindow -> NavigationType.Rail(topLevelDestination)
        else -> NavigationType.Bar(topLevelDestination)
    }
}
