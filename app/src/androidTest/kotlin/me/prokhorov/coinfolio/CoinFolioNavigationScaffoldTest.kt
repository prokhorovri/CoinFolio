package me.prokhorov.coinfolio

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import me.prokhorov.coinfolio.navigation.NavigationType
import me.prokhorov.coinfolio.navigation.TopLevelDestination
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.ui.BottomInset
import me.prokhorov.coinfolio.ui.LocalBottomInset
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Tests the bar and the rail only, so the content is nearly empty and we don't need Hilt.
@RunWith(AndroidJUnit4::class)
class CoinFolioNavigationScaffoldTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private var clickedTab: TopLevelDestination? = null

    // What the screens get from LocalBottomInset.
    private var bottomInset: BottomInset? = null

    private fun showApp(navigationType: NavigationType) {
        composeRule.setContent {
            CoinFolioTheme {
                CoinFolioNavigationScaffold(
                    navigationType = navigationType,
                    onDestinationClick = { clickedTab = it },
                ) {
                    bottomInset = LocalBottomInset.current
                }
            }
        }
    }

    // The item of this tab inside the bar or the rail with this tag.
    private fun item(tag: String, destination: TopLevelDestination): SemanticsNodeInteraction {
        val label = composeRule.activity.getString(destination.labelRes)
        return composeRule.onNode(hasText(label) and hasAnyAncestor(hasTestTag(tag)))
    }

    @Test
    fun bar_mode_shows_the_bar_with_only_the_tab_selected() {
        showApp(NavigationType.Bar(TopLevelDestination.PORTFOLIO))

        composeRule.onNodeWithTag(NAVIGATION_BAR_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(NAVIGATION_RAIL_TAG).assertDoesNotExist()
        item(NAVIGATION_BAR_TAG, TopLevelDestination.PORTFOLIO).assertIsSelected()
        item(NAVIGATION_BAR_TAG, TopLevelDestination.COINS).assertIsNotSelected()
    }

    @Test
    fun rail_mode_shows_the_rail_with_only_the_tab_selected() {
        showApp(NavigationType.Rail(TopLevelDestination.PORTFOLIO))

        composeRule.onNodeWithTag(NAVIGATION_RAIL_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(NAVIGATION_BAR_TAG).assertDoesNotExist()
        item(NAVIGATION_RAIL_TAG, TopLevelDestination.PORTFOLIO).assertIsSelected()
        item(NAVIGATION_RAIL_TAG, TopLevelDestination.COINS).assertIsNotSelected()
    }

    @Test
    fun a_screen_with_no_tab_shows_no_bar_and_no_rail() {
        showApp(NavigationType.None)

        composeRule.onNodeWithTag(NAVIGATION_BAR_TAG).assertDoesNotExist()
        composeRule.onNodeWithTag(NAVIGATION_RAIL_TAG).assertDoesNotExist()
    }

    @Test
    fun a_click_on_a_rail_item_reports_its_tab() {
        showApp(NavigationType.Rail(TopLevelDestination.COINS))

        item(NAVIGATION_RAIL_TAG, TopLevelDestination.PORTFOLIO).performClick()

        composeRule.runOnIdle { assertThat(clickedTab).isEqualTo(TopLevelDestination.PORTFOLIO) }
    }

    @Test
    fun bar_mode_tells_the_screens_a_bottom_bar_covers_the_bottom_edge() {
        showApp(NavigationType.Bar(TopLevelDestination.COINS))

        composeRule.runOnIdle { assertThat(bottomInset?.hasBottomBar).isTrue() }
    }

    @Test
    fun rail_mode_tells_the_screens_no_bottom_bar_covers_the_bottom_edge() {
        showApp(NavigationType.Rail(TopLevelDestination.COINS))

        composeRule.runOnIdle { assertThat(bottomInset?.hasBottomBar).isFalse() }
    }
}
