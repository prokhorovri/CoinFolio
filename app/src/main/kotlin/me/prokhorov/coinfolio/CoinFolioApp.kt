package me.prokhorov.coinfolio

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailDefaults
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.blur.HazeBlurStyle
import dev.chrisbanes.haze.blur.HazeColorEffect
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import me.prokhorov.coinfolio.navigation.AppNavGraph
import me.prokhorov.coinfolio.navigation.NavigationType
import me.prokhorov.coinfolio.navigation.TopLevelDestination
import me.prokhorov.coinfolio.navigation.navigationType
import me.prokhorov.coinfolio.navigation.rememberCoinFolioAppState
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.ui.LocalBottomBarHeight

// Alpha of the bottom bar color over the blurred content.
private const val BOTTOM_BAR_TINT_ALPHA = 0.6f

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val NAVIGATION_BAR_TAG = "navigation_bar"

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val NAVIGATION_RAIL_TAG = "navigation_rail"

// This is the app root. MainActivity calls it once with the whole window.
// So a modifier parameter would never be used.
@Suppress("ModifierMissing")
@Composable
fun CoinFolioApp(windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2()) {
    val appState = rememberCoinFolioAppState()
    val navigationType = navigationType(
        topLevelDestination = appState.currentTopLevelDestination,
        isWideWindow = windowAdaptiveInfo.windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND),
    )

    CoinFolioNavigationScaffold(
        navigationType = navigationType,
        onDestinationClick = appState::navigateToTab,
    ) {
        AppNavGraph(navController = appState.navController)
    }
}

/** The bar or the rail around [content]. A test can pass simple content, with no Hilt. */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
@Composable
internal fun CoinFolioNavigationScaffold(
    navigationType: NavigationType,
    onDestinationClick: (TopLevelDestination) -> Unit,
    content: @Composable () -> Unit,
) {
    val hazeState = rememberHazeState()
    val hasRail = navigationType is NavigationType.Rail

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Every screen handles the window insets in its own Scaffold, so this one
        // must not handle them again. Then innerPadding below is only the bar height.
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            if (navigationType is NavigationType.Bar) {
                CoinFolioNavigationBar(
                    selected = navigationType.selected,
                    hazeState = hazeState,
                    onDestinationClick = onDestinationClick,
                )
            }
        },
    ) { innerPadding ->
        val bottomBarHeight = when (navigationType) {
            is NavigationType.Bar -> innerPadding.calculateBottomPadding()

            // With the rail there is no bottom bar. The list still needs space for the
            // system navigation bar at the bottom.
            is NavigationType.Rail -> {
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
            }

            NavigationType.None -> 0.dp
        }

        CompositionLocalProvider(LocalBottomBarHeight provides bottomBarHeight) {
            // The nav graph is at the same place in both layouts. So the screens keep
            // their saved state, like the scroll position, after a rotation.
            Row(modifier = Modifier.fillMaxSize()) {
                if (hasRail) {
                    CoinFolioNavigationRail(
                        selected = navigationType.selected,
                        onDestinationClick = onDestinationClick,
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        // The rail already adds space for the cutout and the system bar.
                        // So the screens don't add it again.
                        .then(
                            if (hasRail) {
                                Modifier.consumeWindowInsets(
                                    NavigationRailDefaults.windowInsets
                                        .only(WindowInsetsSides.Start),
                                )
                            } else {
                                Modifier
                            },
                        )
                        .hazeSource(hazeState),
                ) {
                    content()
                }
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
        modifier = Modifier
            .testTag(NAVIGATION_BAR_TAG)
            .hazeBlur(input = HazeInput.Sources(hazeState), style = blurStyle),
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

@Composable
private fun CoinFolioNavigationRail(
    selected: TopLevelDestination,
    onDestinationClick: (TopLevelDestination) -> Unit,
) {
    NavigationRail(modifier = Modifier.testTag(NAVIGATION_RAIL_TAG)) {
        TopLevelDestination.entries.forEach { destination ->
            NavigationRailItem(
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

@Preview(showBackground = true)
@Composable
private fun CoinFolioNavigationScaffoldBarPreview() {
    CoinFolioTheme {
        CoinFolioNavigationScaffold(
            navigationType = NavigationType.Bar(TopLevelDestination.COINS),
            onDestinationClick = {},
        ) {}
    }
}

@Preview(showBackground = true, widthDp = 853, heightDp = 400)
@Composable
private fun CoinFolioNavigationScaffoldRailPreview() {
    CoinFolioTheme {
        CoinFolioNavigationScaffold(
            navigationType = NavigationType.Rail(TopLevelDestination.COINS),
            onDestinationClick = {},
        ) {}
    }
}
