package me.prokhorov.coinfolio

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.prokhorov.coinfolio.navigation.AppNavGraph
import me.prokhorov.coinfolio.navigation.TopLevelDestination
import me.prokhorov.coinfolio.navigation.rememberCoinFolioAppState
import me.prokhorov.coinfolio.ui.LocalBottomBarHeight

// This is the app root. MainActivity calls it once with the whole window.
// So a modifier parameter would never be used.
@Suppress("ModifierMissing")
@Composable
fun CoinFolioApp() {
    val appState = rememberCoinFolioAppState()
    val topLevelDestination = appState.currentTopLevelDestination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Every screen handles the window insets in its own Scaffold, so this one
        // must not handle them again. Then innerPadding below is only the bar height.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            topLevelDestination?.let { destination ->
                CoinFolioNavigationBar(
                    selected = destination,
                    onDestinationClick = appState::navigateToTab,
                )
            }
        },
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalBottomBarHeight provides innerPadding.calculateBottomPadding(),
        ) {
            AppNavGraph(navController = appState.navController)
        }
    }
}

@Composable
private fun CoinFolioNavigationBar(
    selected: TopLevelDestination,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = destination == selected,
                onClick = { onDestinationClick(destination) },
                icon = {
                    Icon(painter = painterResource(destination.iconRes), contentDescription = null)
                },
                label = { Text(text = stringResource(destination.labelRes)) },
            )
        }
    }
}
