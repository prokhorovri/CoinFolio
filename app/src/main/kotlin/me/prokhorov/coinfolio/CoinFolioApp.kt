package me.prokhorov.coinfolio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.prokhorov.coinfolio.navigation.AppNavGraph
import me.prokhorov.coinfolio.navigation.TopLevelDestination
import me.prokhorov.coinfolio.navigation.rememberCoinFolioAppState
import me.prokhorov.coinfolio.ui.LocalBottomBarHeight

// Alpha of the bottom bar color over the blurred content.
private const val BOTTOM_BAR_TINT_ALPHA = 0.6f

// This is the app root. MainActivity calls it once with the whole window.
// So a modifier parameter would never be used.
@Suppress("ModifierMissing")
@Composable
fun CoinFolioApp() {
    val appState = rememberCoinFolioAppState()
    val topLevelDestination = appState.currentTopLevelDestination
    val hazeState = rememberHazeState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Every screen handles the window insets in its own Scaffold, so this one
        // must not handle them again. Then innerPadding below is only the bar height.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            topLevelDestination?.let { destination ->
                CoinFolioNavigationBar(
                    selected = destination,
                    hazeState = hazeState,
                    onDestinationClick = appState::navigateToTab,
                )
            }
        },
    ) { innerPadding ->
        CompositionLocalProvider(
            LocalBottomBarHeight provides innerPadding.calculateBottomPadding(),
        ) {
            Box(modifier = Modifier.fillMaxSize().hazeSource(hazeState)) {
                AppNavGraph(navController = appState.navController)
            }
        }
    }
}

@Composable
private fun CoinFolioNavigationBar(
    selected: TopLevelDestination,
    hazeState: HazeState,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    val backgroundColor = MaterialTheme.colorScheme.background
    val tintColor = NavigationBarDefaults.containerColor
    val blurStyle = remember(backgroundColor, tintColor) {
        HazeBlurStyle {
            backgroundColor(backgroundColor)
            colorEffects(
                listOf(HazeColorEffect.tint(tintColor.copy(alpha = BOTTOM_BAR_TINT_ALPHA))),
            )
            // Haze does not blur below Android 12 by default. Without the blur the rows show
            // sharp through the bar and mix with the labels, so the bar stays solid there.
            fallbackColorEffect(HazeColorEffect.tint(tintColor))
        }
    }

    NavigationBar(
        modifier = Modifier.hazeBlur(input = HazeInput.Sources(hazeState), style = blurStyle),
        containerColor = Color.Transparent,
    ) {
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
