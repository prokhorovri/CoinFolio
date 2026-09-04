package me.prokhorov.coinfolio.screen.coinlist

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
import me.prokhorov.coinfolio.util.formatAsUsd
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val BITCOIN_ID = "bitcoin"

// createCoin builds the symbol from the id: the first 3 letters, shown in upper case.
private const val BITCOIN_SYMBOL = "BIT"
private const val BITCOIN_PRICE = "67000.55"
private const val ETHEREUM_ID = "ethereum"

/**
 * Shows the real screen with a real ViewModel over [FakeCoinRepository]. No Hilt: the screen
 * takes the ViewModel as a parameter, so a test can pass its own.
 *
 * Method names use underscores. A space in a method name needs minSdk 30, and this app allows 28.
 */
@RunWith(AndroidJUnit4::class)
class CoinListScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val repository = FakeCoinRepository()

    private var clickedCoinId: String? = null

    private fun showScreen() {
        // Built outside setContent, so a recomposition cannot create a second one.
        val viewModel = CoinListViewModel(GetTopCoinsUseCase(repository))

        composeRule.setContent {
            CoinFolioTheme {
                CoinListScreen(
                    onCoinClick = { clickedCoinId = it },
                    viewModel = viewModel,
                )
            }
        }
    }

    private fun getString(@StringRes id: Int): String = composeRule.activity.getString(id)

    @Test
    fun shows_a_row_for_every_coin() {
        repository.topCoins = Resource.Success(
            listOf(
                createCoin(BITCOIN_ID, price = BITCOIN_PRICE),
                createCoin(ETHEREUM_ID),
            ),
        )

        showScreen()

        composeRule.onNodeWithText(BITCOIN_ID).assertIsDisplayed()
        composeRule.onNodeWithText(BITCOIN_SYMBOL).assertIsDisplayed()
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
    fun an_empty_answer_shows_the_empty_state() {
        repository.topCoins = Resource.Success(emptyList())

        showScreen()

        composeRule
            .onNodeWithText(getString(R.string.coin_list_screen_empty_text))
            .assertIsDisplayed()
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
}
