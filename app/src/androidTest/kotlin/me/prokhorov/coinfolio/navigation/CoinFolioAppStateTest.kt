package me.prokhorov.coinfolio.navigation

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val COIN_ID = "bitcoin"

// Tests the tab state only, so we don't need real screens or Hilt.
@RunWith(AndroidJUnit4::class)
class CoinFolioAppStateTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var appState: CoinFolioAppState

    // What the bottom bar reads. Null means the screen has no tab, so the bar hides.
    private var currentTab: TopLevelDestination? = null

    private fun showNavGraph() {
        composeRule.setContent {
            appState = rememberCoinFolioAppState()
            // Read inside composition, so every navigation writes the new value here.
            currentTab = appState.currentTopLevelDestination

            NavHost(
                navController = appState.navController,
                startDestination = ScreenRoute.CoinList,
            ) {
                composable<ScreenRoute.CoinList> {}
                composable<ScreenRoute.Portfolio> {}
                composable<ScreenRoute.CoinDetail> {}
            }
        }
    }

    private fun navigateToTab(destination: TopLevelDestination) {
        composeRule.runOnUiThread { appState.navigateToTab(destination) }
    }

    private fun assertTab(expected: TopLevelDestination?) {
        composeRule.runOnIdle { assertThat(currentTab).isEqualTo(expected) }
    }

    @Test
    fun starts_on_the_coins_tab() {
        showNavGraph()

        assertTab(TopLevelDestination.COINS)
    }

    @Test
    fun a_click_on_a_tab_opens_it() {
        showNavGraph()

        navigateToTab(TopLevelDestination.PORTFOLIO)

        assertTab(TopLevelDestination.PORTFOLIO)
    }

    // The coin detail is not a tab, so the bottom bar hides there.
    @Test
    fun a_screen_with_no_tab_reports_no_tab() {
        showNavGraph()

        composeRule.runOnUiThread {
            appState.navController.navigate(ScreenRoute.CoinDetail(id = COIN_ID))
        }

        assertTab(null)
    }

    @Test
    fun a_tab_is_not_opened_twice() {
        showNavGraph()

        navigateToTab(TopLevelDestination.PORTFOLIO)
        navigateToTab(TopLevelDestination.COINS)

        // Only the current screen is left. The back button leaves the app, it does not walk
        // back through every tab the user opened.
        composeRule.runOnIdle {
            assertThat(appState.navController.previousBackStackEntry).isNull()
        }
    }
}
