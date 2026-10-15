package me.prokhorov.coinfolio.screen.coinlist

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.createCoin
import me.prokhorov.coinfolio.domain.usecase.GetTopCoinsUseCase
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.ListMaxWidth
import me.prokhorov.coinfolio.ui.BottomInset
import me.prokhorov.coinfolio.ui.LocalBottomInset
import me.prokhorov.coinfolio.ui.overlay.BOTTOM_FADE_TAG
import me.prokhorov.coinfolio.ui.overlay.TOP_FADE_TAG
import me.prokhorov.coinfolio.util.formatAsUsd
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val BITCOIN_ID = "bitcoin"
private const val BITCOIN_SYMBOL = "btc"
private const val BITCOIN_PRICE = "67000.55"
private const val ETHEREUM_ID = "ethereum"

// A landscape tablet, wider than ListMaxWidth.
private val WIDE_WINDOW_SIZE = DpSize(1280.dp, 800.dp)

/**
 * Shows the real screen with a real ViewModel over [FakeCoinRepository]. No Hilt: the screen
 * takes the ViewModel as a parameter. A test can pass its own. Method names use underscores.
 * A space in a method name needs minSdk 30. This app allows 28.
 */
@RunWith(AndroidJUnit4::class)
class CoinListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val repository = FakeCoinRepository()

    private var clickedCoinId: String? = null

    // In portrait mode, the bottom bar covers the bottom edge.
    private fun showScreen(hasBottomBar: Boolean = true, windowSize: DpSize? = null) {
        // Built outside setContent, so a recomposition cannot create a second one.
        val viewModel = CoinListViewModel(GetTopCoinsUseCase(repository))

        composeRule.setContent {
            CoinFolioTheme {
                CompositionLocalProvider(
                    LocalBottomInset provides BottomInset(
                        padding = 0.dp,
                        hasBottomBar = hasBottomBar,
                    ),
                ) {
                    val screen = @Composable {
                        CoinListScreen(
                            onCoinClick = { clickedCoinId = it },
                            viewModel = viewModel,
                        )
                    }
                    if (windowSize == null) {
                        screen()
                    } else {
                        // Gives the screen a window of this size, even if the device is smaller.
                        DeviceConfigurationOverride(
                            DeviceConfigurationOverride.ForcedSize(windowSize),
                        ) {
                            screen()
                        }
                    }
                }
            }
        }
    }

    private fun getString(@StringRes id: Int): String = composeRule.activity.getString(id)

    private fun searchField() = composeRule.onNode(hasSetTextAction())

    @Test
    fun shows_a_row_for_every_coin() {
        repository.topCoins = Resource.Success(
            listOf(
                createCoin(BITCOIN_ID, BITCOIN_SYMBOL, price = BITCOIN_PRICE),
                createCoin(ETHEREUM_ID),
            ),
        )

        showScreen()

        composeRule.onNodeWithText(BITCOIN_ID).assertIsDisplayed()
        composeRule.onNodeWithText(BITCOIN_SYMBOL.uppercase()).assertIsDisplayed()
        composeRule.onNodeWithText(BigDecimal(BITCOIN_PRICE).formatAsUsd()).assertIsDisplayed()
        composeRule.onNodeWithText(ETHEREUM_ID).assertIsDisplayed()
    }

    @Test
    fun a_click_on_a_row_reports_the_coin_id() {
        repository.topCoins = Resource.Success(
            listOf(
                createCoin(BITCOIN_ID),
                createCoin(ETHEREUM_ID),
            ),
        )
        showScreen()

        composeRule.onNodeWithText(ETHEREUM_ID).performClick()

        assertThat(clickedCoinId).isEqualTo(ETHEREUM_ID)
    }

    @Test
    fun a_row_is_not_wider_than_the_list_max_width_on_a_wide_window() {
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))

        showScreen(hasBottomBar = false, windowSize = WIDE_WINDOW_SIZE)

        val rowWidth = composeRule.onNodeWithText(BITCOIN_ID).getUnclippedBoundsInRoot().width
        assertThat(rowWidth).isAtMost(ListMaxWidth)
    }

    @Test
    fun the_search_field_is_not_wider_than_the_list_max_width_on_a_wide_window() {
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))

        showScreen(hasBottomBar = false, windowSize = WIDE_WINDOW_SIZE)

        val fieldWidth = searchField().getUnclippedBoundsInRoot().width
        assertThat(fieldWidth).isAtMost(ListMaxWidth)
    }

    @Test
    fun an_empty_answer_shows_the_empty_state() {
        repository.topCoins = Resource.Success(emptyList())

        showScreen()

        composeRule
            .onNodeWithText(getString(R.string.coin_list_screen_empty_text))
            .assertIsDisplayed()
    }

    @Test
    fun the_list_has_a_top_fade() {
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))

        showScreen()

        composeRule.onNodeWithTag(TOP_FADE_TAG).assertExists()
    }

    // At rest the first row is not under the fade.
    @Test
    fun the_top_fade_ends_above_the_first_row() {
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))

        showScreen()

        val fadeBottom = composeRule.onNodeWithTag(TOP_FADE_TAG).getUnclippedBoundsInRoot().bottom
        val rowTop = composeRule.onNodeWithText(BITCOIN_ID).getUnclippedBoundsInRoot().top
        assertThat(fadeBottom.value).isAtMost(rowTop.value)
    }

    // The fade is only for content that scrolls.
    @Test
    fun the_empty_state_has_no_top_fade() {
        repository.topCoins = Resource.Success(emptyList())

        showScreen()

        composeRule.onNodeWithTag(TOP_FADE_TAG).assertDoesNotExist()
    }

    // The rail doesn't cover the bottom edge, so the list fades there.
    @Test
    fun the_list_has_a_bottom_fade_when_no_bottom_bar_covers_the_bottom() {
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))

        showScreen(hasBottomBar = false)

        composeRule.onNodeWithTag(BOTTOM_FADE_TAG).assertExists()
    }

    @Test
    fun the_list_has_no_bottom_fade_under_a_bottom_bar() {
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))

        showScreen(hasBottomBar = true)

        composeRule.onNodeWithTag(BOTTOM_FADE_TAG).assertDoesNotExist()
    }

    // No coins yet, so the error takes the whole screen and offers a retry.
    @Test
    fun a_failure_with_no_coins_shows_the_error_and_a_retry_button() {
        repository.topCoins = Resource.Failure(AppError.RateLimited)

        showScreen()

        composeRule.onNodeWithText(getString(R.string.rate_limit_error_text)).assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.retry_text)).assertIsDisplayed()
    }

    @Test
    fun a_click_on_retry_loads_the_coins_again() {
        repository.topCoins = Resource.Failure(AppError.Network)
        showScreen()
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))

        composeRule.onNodeWithText(getString(R.string.retry_text)).performClick()

        composeRule.onNodeWithText(BITCOIN_ID).assertIsDisplayed()
    }

    @Test
    fun a_query_shows_only_the_matching_coins() {
        repository.topCoins = Resource.Success(
            listOf(
                createCoin(BITCOIN_ID),
                createCoin(ETHEREUM_ID),
            ),
        )
        showScreen()

        searchField().performTextInput("bitc")

        composeRule.onNodeWithText(BITCOIN_ID).assertIsDisplayed()
        composeRule.onAllNodesWithText(ETHEREUM_ID).assertCountEquals(0)
    }

    // The field stays on screen next to the message, so the user can fix the query.
    @Test
    fun a_query_with_no_match_shows_the_not_found_message() {
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))
        showScreen()

        searchField().performTextInput("xyz")

        val message = composeRule.activity
            .getString(R.string.coin_list_screen_not_found_text, "xyz")
        composeRule.onNodeWithText(message).assertIsDisplayed()
        searchField().assertIsDisplayed()
    }

    @Test
    fun the_search_key_clears_the_focus() {
        repository.topCoins = Resource.Success(listOf(createCoin(BITCOIN_ID)))
        showScreen()
        searchField().performTextInput("bitc")

        searchField().performImeAction()

        composeRule.onAllNodes(isFocused()).assertCountEquals(0)
    }

    @Test
    fun a_scroll_clears_the_focus() {
        repository.topCoins = Resource.Success(List(30) { createCoin("coin$it") })
        showScreen()
        searchField().performTextInput("coin")

        composeRule.onNode(hasScrollAction()).performTouchInput { swipeUp() }

        composeRule.onAllNodes(isFocused()).assertCountEquals(0)
    }
}
