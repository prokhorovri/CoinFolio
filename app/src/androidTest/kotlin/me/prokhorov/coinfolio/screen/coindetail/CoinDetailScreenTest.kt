package me.prokhorov.coinfolio.screen.coindetail

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.FakePortfolioRepository
import me.prokhorov.coinfolio.domain.testing.createCoinDetail
import me.prokhorov.coinfolio.domain.usecase.GetCoinDetailUseCase
import me.prokhorov.coinfolio.domain.usecase.GetHoldingUseCase
import me.prokhorov.coinfolio.domain.usecase.SaveHoldingUseCase
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.util.formatAsUsd
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val COIN_ID = "bitcoin"

// createCoinDetail takes the symbol from the id: the first 3 letters, in upper case.
private const val COIN_SYMBOL = "BIT"
private const val COIN_PRICE = "67000.55"

// A whole number, so the locale of the emulator cannot break the test.
private const val QUANTITY = "2"

// One digit over the limit. The field keeps the text, and the state shows the error.
private const val TOO_LARGE_QUANTITY = "1234567890123456"

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
    private val portfolioRepository = FakePortfolioRepository()

    private var isBackClicked = false

    private fun showScreen() {
        // Built outside setContent, so a recomposition cannot create a second one.
        val viewModel = CoinDetailViewModel(
            getCoinDetailUseCase = GetCoinDetailUseCase(coinRepository),
            getHoldingUseCase = GetHoldingUseCase(portfolioRepository),
            saveHoldingUseCase = SaveHoldingUseCase(portfolioRepository),
            savedStateHandle = SavedStateHandle(mapOf("id" to COIN_ID)),
        )

        composeRule.setContent {
            CoinFolioTheme {
                CoinDetailScreen(
                    onBackClick = { isBackClicked = true },
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun makeCoinAvailable() {
        coinRepository.coinDetails = mapOf(
            COIN_ID to Resource.Success(createCoinDetail(COIN_ID, price = COIN_PRICE)),
        )
    }

    private fun getString(@StringRes id: Int): String = composeRule.activity.getString(id)

    @Test
    fun shows_the_coin_summary() {
        makeCoinAvailable()

        showScreen()

        composeRule.onNodeWithText(COIN_ID).assertIsDisplayed()
        composeRule.onNodeWithText(COIN_SYMBOL).assertIsDisplayed()
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

        composeRule.onNodeWithText("$QUANTITY $COIN_SYMBOL").assertIsDisplayed()
        composeRule
            .onNodeWithText(getString(R.string.coin_detail_screen_saved_text))
            .assertIsDisplayed()
    }

    @Test
    fun a_click_on_back_reports_it() {
        makeCoinAvailable()
        showScreen()

        composeRule.onNodeWithContentDescription(getString(R.string.back_text)).performClick()

        assertThat(isBackClicked).isTrue()
    }
}
