package me.prokhorov.coinfolio.screen.coindetail

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material3.adaptive.Posture
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsActions.ScrollBy
import androidx.compose.ui.semantics.SemanticsProperties.VerticalScrollAxisRange
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.ForcedSize
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.window.core.layout.WindowSizeClass
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.component.BOTTOM_FADE_TAG
import me.prokhorov.coinfolio.component.TOP_FADE_TAG
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.FakeHoldingRepository
import me.prokhorov.coinfolio.domain.testing.createCoinDetail
import me.prokhorov.coinfolio.domain.usecase.GetCoinDetailUseCase
import me.prokhorov.coinfolio.domain.usecase.GetHoldingUseCase
import me.prokhorov.coinfolio.domain.usecase.SaveHoldingUseCase
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.Spacing24
import me.prokhorov.coinfolio.util.formatAsUsd
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val COIN_ID = "bitcoin"
private const val COIN_SYMBOL = "btc"
private const val COIN_PRICE = "67000.55"

// A whole number, so the locale of the emulator cannot break the test.
private const val QUANTITY = "2"

// One digit over the limit. The field keeps the text, and the state shows the error.
private const val TOO_LARGE_QUANTITY = "1234567890123456"

// Shorter than the content, so the content must scroll.
private val SHORT_SCREEN_HEIGHT = 300.dp

// Much less than the content can scroll on the short screen.
private const val SMALL_SCROLL_PIXELS = 100f

// A phone in landscape, so the screen shows two panes.
private val WIDE_WINDOW_SIZE = DpSize(891.dp, 411.dp)

/**
 * Shows the real screen with a real ViewModel over the fakes. The coin id comes from
 * [SavedStateHandle], like in the app. Method names use underscores. A space in a method name
 * needs minSdk 30. This app allows 28.
 */
@RunWith(AndroidJUnit4::class)
class CoinDetailScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val coinRepository = FakeCoinRepository()
    private val holdingRepository = FakeHoldingRepository()

    private var isBackClicked = false

    // A short screen makes the content scroll on any test device.
    private fun showScreen(height: Dp? = null, windowSize: DpSize? = null) {
        // Built outside setContent, so a recomposition cannot create a second one.
        val viewModel = CoinDetailViewModel(
            getCoinDetailUseCase = GetCoinDetailUseCase(coinRepository),
            getHoldingUseCase = GetHoldingUseCase(holdingRepository),
            saveHoldingUseCase = SaveHoldingUseCase(holdingRepository),
            savedStateHandle = SavedStateHandle(mapOf("id" to COIN_ID)),
        )

        composeRule.setContent {
            CoinFolioTheme {
                Box(modifier = if (height != null) Modifier.height(height) else Modifier) {
                    if (windowSize == null) {
                        CoinDetailScreen(
                            onBackClick = { isBackClicked = true },
                            viewModel = viewModel,
                        )
                    } else {
                        // The screen gets a window of this size and the matching size class.
                        DeviceConfigurationOverride(
                            DeviceConfigurationOverride.ForcedSize(windowSize),
                        ) {
                            CoinDetailScreen(
                                onBackClick = { isBackClicked = true },
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

    private fun makeCoinAvailable() {
        coinRepository.coinDetails = mapOf(
            COIN_ID to Resource.Success(createCoinDetail(COIN_ID, COIN_SYMBOL, price = COIN_PRICE)),
        )
    }

    private fun getString(@StringRes id: Int): String = composeRule.activity.getString(id)

    // The quantity field can scroll too, so it is left out.
    private fun scrollContentBy(pixels: Float) {
        composeRule
            .onNode(hasScrollAction() and !hasSetTextAction())
            .performSemanticsAction(ScrollBy) { scrollBy -> scrollBy(0f, pixels) }
    }

    @Test
    fun shows_the_coin_summary() {
        makeCoinAvailable()

        showScreen()

        composeRule.onNodeWithText(COIN_ID).assertIsDisplayed()
        composeRule.onNodeWithText(COIN_SYMBOL.uppercase()).assertIsDisplayed()
        composeRule.onNodeWithText(BigDecimal(COIN_PRICE).formatAsUsd()).assertIsDisplayed()
    }

    // The fake has no coin here, so the answer is NotFound.
    @Test
    fun an_unknown_coin_shows_the_error_and_a_retry_button() {
        showScreen()

        composeRule
            .onNodeWithText(getString(R.string.coin_not_found_error_text))
            .assertIsDisplayed()
        composeRule.onNodeWithText(getString(R.string.retry_text)).assertIsDisplayed()
    }

    @Test
    fun a_valid_quantity_turns_the_save_button_on() {
        makeCoinAvailable()
        showScreen()

        composeRule.onNode(hasSetTextAction()).performTextInput(QUANTITY)

        composeRule
            .onNodeWithText(getString(R.string.coin_detail_screen_save_text))
            .assertIsEnabled()
    }

    @Test
    fun a_too_large_quantity_shows_the_reason_and_keeps_the_save_button_off() {
        makeCoinAvailable()
        showScreen()

        composeRule.onNode(hasSetTextAction()).performTextInput(TOO_LARGE_QUANTITY)

        composeRule
            .onNodeWithText(getString(R.string.coin_detail_screen_quantity_too_large_text))
            .assertIsDisplayed()
        composeRule
            .onNodeWithText(getString(R.string.coin_detail_screen_save_text))
            .assertIsNotEnabled()
    }

    // The holding line comes from the flow, the snackbar from the effect. This test checks both.
    @Test
    fun a_save_shows_the_new_holding_and_a_snackbar() {
        makeCoinAvailable()
        showScreen()
        composeRule.onNode(hasSetTextAction()).performTextInput(QUANTITY)

        composeRule.onNodeWithText(getString(R.string.coin_detail_screen_save_text)).performClick()

        composeRule.onNodeWithText("$QUANTITY ${COIN_SYMBOL.uppercase()}").assertIsDisplayed()
        composeRule
            .onNodeWithText(getString(R.string.coin_detail_screen_saved_text))
            .assertIsDisplayed()
    }

    // The holding has its own pane, so the field is on screen next to the market data.
    @Test
    fun a_wide_window_shows_the_quantity_field_to_the_right_of_the_market_data() {
        makeCoinAvailable()

        showScreen(windowSize = WIDE_WINDOW_SIZE)

        val marketData = composeRule
            .onNodeWithText(getString(R.string.coin_detail_screen_market_data_title_text))
        val quantityField = composeRule.onNode(hasSetTextAction())
        marketData.assertIsDisplayed()
        quantityField.assertIsDisplayed()
        val marketDataRight = marketData.getUnclippedBoundsInRoot().right
        assertThat(marketDataRight).isAtMost(quantityField.getUnclippedBoundsInRoot().left)
    }

    @Test
    fun a_save_on_a_wide_window_shows_the_new_holding() {
        makeCoinAvailable()
        showScreen(windowSize = WIDE_WINDOW_SIZE)
        composeRule.onNode(hasSetTextAction()).performTextInput(QUANTITY)

        composeRule.onNodeWithText(getString(R.string.coin_detail_screen_save_text)).performClick()

        composeRule.onNodeWithText("$QUANTITY ${COIN_SYMBOL.uppercase()}").assertIsDisplayed()
    }

    // The end of the content has room for the fade. So at the end, Save is not under it.
    @Test
    fun at_the_end_of_the_scroll_save_is_above_the_bottom_fade() {
        makeCoinAvailable()
        showScreen(height = SHORT_SCREEN_HEIGHT)

        // Two nodes can scroll: the content and the quantity field (its text scrolls sideways).
        // The quantity field is left out.
        val content = composeRule.onNode(hasScrollAction() and !hasSetTextAction())
        // Scrolls straight to the end on any screen size.
        // Do not use swipeUp() here: after a swipe, composeRule.activity hung the test.
        // Scrolls straight to the end.
        val maxScroll = content.fetchSemanticsNode().config[VerticalScrollAxisRange].maxValue()
        content.performSemanticsAction(ScrollBy) { scrollBy -> scrollBy(0f, maxScroll) }

        val saveBottom = composeRule
            .onNodeWithText(getString(R.string.coin_detail_screen_save_text))
            .fetchSemanticsNode()
            .boundsInRoot
            .bottom
        val fadeTop = composeRule
            .onNodeWithTag(BOTTOM_FADE_TAG)
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        // In pixels, the same unit the layout uses, so both sides are rounded the same way.
        val columnPadding = with(composeRule.density) { Spacing24.roundToPx() }
        // The Column has its own Spacing24 below Save. Without the room for the fade, Save would
        // end right at the fade top.
        assertThat(saveBottom).isAtMost(fadeTop - columnPadding)
    }

    // The content scrolls under the top bar, so it has a fade.
    @Test
    fun the_content_has_a_top_fade() {
        makeCoinAvailable()

        showScreen()

        composeRule.onNodeWithTag(TOP_FADE_TAG).assertExists()
    }

    // At rest, nothing is under the bar.
    @Test
    fun the_content_starts_below_the_top_bar() {
        makeCoinAvailable()

        showScreen()

        val barBottom = composeRule.onNodeWithTag(TOP_BAR_TAG).getUnclippedBoundsInRoot().bottom
        val summaryTop = composeRule
            .onNodeWithTag(COIN_SUMMARY_TAG)
            .getUnclippedBoundsInRoot()
            .top
        assertThat(barBottom.value).isAtMost(summaryTop.value)
    }

    // The fade is only for content that scrolls.
    @Test
    fun the_error_state_has_no_top_fade() {
        showScreen()

        composeRule.onNodeWithTag(TOP_FADE_TAG).assertDoesNotExist()
    }

    @Test
    fun a_click_on_back_reports_it() {
        makeCoinAvailable()
        showScreen()

        composeRule.onNodeWithContentDescription(getString(R.string.back_text)).performClick()

        assertThat(isBackClicked).isTrue()
    }

    // The title is the coin name. The test coin uses its id as the name.
    @Test
    fun a_scroll_hides_the_title() {
        makeCoinAvailable()
        showScreen(height = SHORT_SCREEN_HEIGHT)

        scrollContentBy(SMALL_SCROLL_PIXELS)

        composeRule.onNodeWithText(COIN_ID).assertDoesNotExist()
    }

    @Test
    fun a_scroll_back_to_the_top_shows_the_title() {
        makeCoinAvailable()
        showScreen(height = SHORT_SCREEN_HEIGHT)
        scrollContentBy(SMALL_SCROLL_PIXELS)

        scrollContentBy(-SMALL_SCROLL_PIXELS)

        composeRule.onNodeWithText(COIN_ID).assertIsDisplayed()
    }

    @Test
    fun a_click_on_back_after_a_scroll_reports_it() {
        makeCoinAvailable()
        showScreen(height = SHORT_SCREEN_HEIGHT)
        scrollContentBy(SMALL_SCROLL_PIXELS)

        composeRule.onNodeWithContentDescription(getString(R.string.back_text)).performClick()

        assertThat(isBackClicked).isTrue()
    }
}
