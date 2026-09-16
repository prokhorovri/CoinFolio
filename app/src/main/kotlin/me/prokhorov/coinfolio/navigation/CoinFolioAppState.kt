package me.prokhorov.coinfolio.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

@Composable
fun rememberCoinFolioAppState(
    navController: NavHostController = rememberNavController(),
): CoinFolioAppState {
    return remember(navController) { CoinFolioAppState(navController) }
}

/** App level navigation state. */
@Stable
class CoinFolioAppState(val navController: NavHostController) {

    // The tab the user is on. Null on a screen that has no tab, like the coin detail.
    val currentTopLevelDestination: TopLevelDestination?
        @Composable get() {
            val backStackEntry by navController.currentBackStackEntryAsState()
            return backStackEntry?.destination?.toTopLevelDestination()
        }

    /**
     * One back stack entry per tab. `saveState` keeps the state of the tab the user leaves, and
     * `restoreState` brings it back.
     */
    fun navigateToTab(destination: TopLevelDestination) {
        navController.navigate(destination.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

// Null when this destination is not a tab.
private fun NavDestination.toTopLevelDestination(): TopLevelDestination? {
    return TopLevelDestination.entries.firstOrNull { tab ->
        hierarchy.any { it.hasRoute(tab.route::class) }
    }
}
