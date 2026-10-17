package me.prokhorov.coinfolio.screen.portfolio

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.core.layout.WindowSizeClass
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.coroutines.runBlocking
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.component.BOTTOM_FADE_TAG
import me.prokhorov.coinfolio.component.TOP_FADE_TAG
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.FakeHoldingRepository
import me.prokhorov.coinfolio.domain.testing.createCoin
import me.prokhorov.coinfolio.domain.testing.createHolding
import me.prokhorov.coinfolio.domain.usecase.GetCoinsByIdsUseCase
import me.prokhorov.coinfolio.domain.usecase.GetHoldingsUseCase
import me.prokhorov.coinfolio.domain.usecase.GetPortfolioUseCase
import me.prokhorov.coinfolio.layout.BottomInset
import me.prokhorov.coinfolio.layout.LocalBottomInset
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.util.formatAsUsd
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val BITCOIN_ID = "bitcoin"
private const val BITCOIN_SYMBOL = "btc"
private const val BITCOIN_PRICE = "100"
private const val BITCOIN_QUANTITY = "2"
private const val BITCOIN_VALUE = "200"

private const val ETHEREUM_ID = "ethereum"
private const val ETHEREUM_SYMBOL = "eth"
private const val ETHEREUM_PRICE = "50"
private const val ETHEREUM_QUANTITY = "3"

// 2 bitcoin at $100 plus 3 ethereum at $50.
private const val TOTAL_VALUE = "350"

// The market call answers with no coin for this one, so the row has no price and no value.
private const val UNKNOWN_ID = "solana"
private const val UNKNOWN_QUANTITY = "7"

// The value and the price of a row with no coin.
private const val DASH_COUNT = 2

// A landscape tablet, so the screen shows two panes.
private val WIDE_WINDOW_SIZE = DpSize(1280.dp, 800.dp)

/*
 * Shows the real screen with a real ViewModel over the fakes. No Hilt: the screen takes the
 * ViewModel as a parameter.
 */
@RunWith(AndroidJUnit4::class)
class PortfolioScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val holdingRepository = FakeHoldingRepository()
    private val coinRepository = FakeCoinRepository()

    private var clickedCoinId: String? = null

    // In portrait mode, the bottom bar covers the bottom edge.
    private fun showScreen(hasBottomBar: Boolean = true, windowSize: DpSize? = null) {
        // Built outside setContent, so a recomposition cannot create a second one.
        val viewModel = PortfolioViewModel(
            GetPortfolioUseCase(
                GetHoldingsUseCase(holdingRepository),
                GetCoinsByIdsUseCase(coinRepository),
            ),
        )

        composeRule.setContent {
            CoinFolioTheme {
                CompositionLocalProvider(
                    LocalBottomInset provides BottomInset(
                        padding = 0.dp,
                        hasBottomBar = hasBottomBar,
                    ),
                ) {
                    if (windowSize == null) {
                        PortfolioScreen(
                            onCoinClick = { clickedCoinId = it },
                            viewModel = viewModel,
                        )
                    } else {
                        // The screen gets a window of this size and the matching size class.
                        DeviceConfigurationOverride(
                            DeviceConfigurationOverride.ForcedSize(windowSize),
                        ) {
                            PortfolioScreen(
                                onCoinClick = { clickedCoinId = it },
                                viewModel = viewModel,
                                windowAdaptiveInfo = windowAdaptiveInfo(windowSize),
                            )
                        }
                    }
                }
            }
        }
    }

    private fun windowAdaptiveInfo(windowSize: DpSize): WindowAdaptiveInfo {
        val sizeClass = WindowSizeClass(
            minWidthDp = windowSize.width.value.toInt(),
            minHeightDp = windowSize.height.value.toInt(),
        )
        return WindowAdaptiveInfo(windowSizeClass = sizeClass, windowPosture = Posture())
    }

    private fun saveHolding(coinId: String, quantity: String) {
        runBlocking { holdingRepository.saveHolding(createHolding(coinId, quantity)) }
    }

    private fun makeCoinsAvailable() {
        coinRepository.marketCoins = mapOf(
            BITCOIN_ID to createCoin(BITCOIN_ID, BITCOIN_SYMBOL, price = BITCOIN_PRICE),
            ETHEREUM_ID to createCoin(ETHEREUM_ID, ETHEREUM_SYMBOL, price = ETHEREUM_PRICE),
        )
    }

    private fun getString(@StringRes id: Int, vararg args: Any): String {
        return composeRule.activity.getString(id, *args)
    }

    private fun totalText(): String = BigDecimal(TOTAL_VALUE).formatAsUsd()

    // The total as an item inside the list that scrolls.
    private fun totalInList() = composeRule.onNode(
        hasText(totalText()) and hasAnyAncestor(hasScrollAction()),
    )

    private fun quantityText(quantity: String, symbol: String): String {
        return getString(R.string.portfolio_screen_quantity_text, quantity, symbol)
    }

    @Test
    fun shows_a_row_for_every_holding() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        saveHolding(ETHEREUM_ID, ETHEREUM_QUANTITY)
        makeCoinsAvailable()

        showScreen()

        composeRule.onNodeWithText(BITCOIN_ID).assertIsDisplayed()
        composeRule
            .onNodeWithText(quantityText(BITCOIN_QUANTITY, BITCOIN_SYMBOL.uppercase()))
            .assertIsDisplayed()
        composeRule.onNodeWithText(ETHEREUM_ID).assertIsDisplayed()
        composeRule
            .onNodeWithText(quantityText(ETHEREUM_QUANTITY, ETHEREUM_SYMBOL.uppercase()))
            .assertIsDisplayed()
    }

    @Test
    fun shows_the_value_of_a_row_and_the_total_of_them_all() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        saveHolding(ETHEREUM_ID, ETHEREUM_QUANTITY)
        makeCoinsAvailable()

        showScreen()

        composeRule.onNodeWithText(BigDecimal(BITCOIN_PRICE).formatAsUsd()).assertIsDisplayed()
        composeRule.onNodeWithText(BigDecimal(BITCOIN_VALUE).formatAsUsd()).assertIsDisplayed()
        composeRule
            .onNodeWithText(getString(R.string.portfolio_screen_total_label_text))
            .assertIsDisplayed()
        composeRule.onNodeWithText(totalText()).assertIsDisplayed()
    }

    @Test
    fun a_narrow_window_shows_the_total_inside_the_list() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        saveHolding(ETHEREUM_ID, ETHEREUM_QUANTITY)
        makeCoinsAvailable()

        showScreen()

        totalInList().assertIsDisplayed()
    }

    // The total stays on screen while the list scrolls.
    @Test
    fun a_wide_window_shows_the_total_outside_the_list() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        saveHolding(ETHEREUM_ID, ETHEREUM_QUANTITY)
        makeCoinsAvailable()

        showScreen(hasBottomBar = false, windowSize = WIDE_WINDOW_SIZE)

        composeRule.onNodeWithText(totalText()).assertIsDisplayed()
        totalInList().assertDoesNotExist()
    }

    @Test
    fun a_wide_window_shows_the_total_to_the_left_of_the_rows() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        saveHolding(ETHEREUM_ID, ETHEREUM_QUANTITY)
        makeCoinsAvailable()

        showScreen(hasBottomBar = false, windowSize = WIDE_WINDOW_SIZE)

        val totalRight = composeRule.onNodeWithText(totalText()).getUnclippedBoundsInRoot().right
        val rowLeft = composeRule.onNodeWithText(BITCOIN_ID).getUnclippedBoundsInRoot().left
        assertThat(totalRight).isAtMost(rowLeft)
    }

    // The market call can miss a coin. The quantity is still worth showing.
    @Test
    fun a_holding_with_no_market_data_shows_the_quantity_and_dashes() {
        saveHolding(UNKNOWN_ID, UNKNOWN_QUANTITY)

        showScreen()

        composeRule.onNodeWithText(UNKNOWN_ID).assertIsDisplayed()
        composeRule.onNodeWithText(UNKNOWN_QUANTITY).assertIsDisplayed()
        // A row is clickable, so Compose merges its texts into one node. The unmerged tree
        // keeps each Text on its own, so both dashes can be counted.
        composeRule
            .onAllNodesWithText(getString(R.string.no_value_text), useUnmergedTree = true)
            .assertCountEquals(DASH_COUNT)
    }

    @Test
    fun no_holdings_shows_the_empty_state() {
        showScreen()

        composeRule
            .onNodeWithText(getString(R.string.portfolio_screen_empty_text))
            .assertIsDisplayed()
    }

    @Test
    fun the_list_has_a_top_fade() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        makeCoinsAvailable()

        showScreen()

        composeRule.onNodeWithTag(TOP_FADE_TAG).assertExists()
    }

    // The fade is only for content that scrolls.
    @Test
    fun the_empty_state_has_no_top_fade() {
        showScreen()

        composeRule.onNodeWithTag(TOP_FADE_TAG).assertDoesNotExist()
    }

    // The rail doesn't cover the bottom edge, so the list fades there.
    @Test
    fun the_list_has_a_bottom_fade_when_no_bottom_bar_covers_the_bottom() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        makeCoinsAvailable()

        showScreen(hasBottomBar = false)

        composeRule.onNodeWithTag(BOTTOM_FADE_TAG).assertExists()
    }

    @Test
    fun the_list_has_no_bottom_fade_under_a_bottom_bar() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        makeCoinsAvailable()

        showScreen(hasBottomBar = true)

        composeRule.onNodeWithTag(BOTTOM_FADE_TAG).assertDoesNotExist()
    }

    // No rows yet, so the error takes the whole screen and offers a retry.
    @Test
    fun a_failure_with_no_rows_shows_the_error_and_a_retry_button() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        coinRepository.marketError = AppError.RateLimited

        showScreen()

        composeRule.onNodeWithText(getString(R.string.rate_limit_error_text)).assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.retry_text)).assertIsDisplayed()
    }

    @Test
    fun a_click_on_retry_shows_the_rows() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        coinRepository.marketError = AppError.Network
        showScreen()
        coinRepository.marketError = null
        makeCoinsAvailable()

        composeRule.onNodeWithText(getString(R.string.retry_text)).performClick()

        composeRule.onNodeWithText(BITCOIN_ID).assertIsDisplayed()
    }

    @Test
    fun a_click_on_a_row_reports_the_coin_id() {
        saveHolding(BITCOIN_ID, BITCOIN_QUANTITY)
        saveHolding(ETHEREUM_ID, ETHEREUM_QUANTITY)
        makeCoinsAvailable()
        showScreen()

        composeRule.onNodeWithText(ETHEREUM_ID).performClick()

        assertThat(clickedCoinId).isEqualTo(ETHEREUM_ID)
    }
}
