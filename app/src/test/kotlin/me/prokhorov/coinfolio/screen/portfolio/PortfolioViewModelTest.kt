package me.prokhorov.coinfolio.screen.portfolio

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.FakeHoldingRepository
import me.prokhorov.coinfolio.domain.testing.createCoin
import me.prokhorov.coinfolio.domain.testing.createHolding
import me.prokhorov.coinfolio.domain.usecase.GetCoinsByIdsUseCase
import me.prokhorov.coinfolio.domain.usecase.GetHoldingsUseCase
import me.prokhorov.coinfolio.domain.usecase.GetPortfolioUseCase
import me.prokhorov.coinfolio.testing.MainDispatcherRule
import org.junit.Rule
import org.junit.Test

private const val BITCOIN = "bitcoin"

class PortfolioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val holdingRepository = FakeHoldingRepository()
    private val coinRepository = FakeCoinRepository()

    private fun viewModel(): PortfolioViewModel {
        return PortfolioViewModel(
            GetPortfolioUseCase(
                GetHoldingsUseCase(holdingRepository),
                GetCoinsByIdsUseCase(coinRepository),
            ),
        )
    }

    @Test
    fun `loads the portfolio on start`() = runTest {
        holdingRepository.saveHolding(createHolding(BITCOIN, "2"))
        coinRepository.marketCoins = mapOf(BITCOIN to createCoin(BITCOIN, price = "100"))

        val state = viewModel().state.value

        assertThat(state.portfolio.valuedHoldings.map { it.holding.coinId })
            .containsExactly(BITCOIN)
        assertThat(state.portfolio.totalValue).isEqualToIgnoringScale("200")
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `an empty portfolio is a success, not an error`() = runTest {
        val state = viewModel().state.value

        assertThat(state.portfolio.valuedHoldings).isEmpty()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    // The screen has nothing to show yet, so the error fills the whole screen. It belongs in the
    // state. A snackbar would only say the same thing again.
    @Test
    fun `a market failure with no rows goes to the state`() = runTest {
        holdingRepository.saveHolding(createHolding(BITCOIN, "2"))
        coinRepository.marketError = AppError.RateLimited

        val state = viewModel().state.value

        assertThat(state.error).isEqualTo(AppError.RateLimited)
        assertThat(state.isLoading).isFalse()
    }

    // A read failure ends the holdings flow, so this error also stops the updates. The screen is
    // still empty, so it shows the error and a retry button.
    @Test
    fun `a storage failure with no rows goes to the state`() = runTest {
        holdingRepository.error = AppError.Storage

        val state = viewModel().state.value

        assertThat(state.error).isEqualTo(AppError.Storage)
        assertThat(state.isLoading).isFalse()
    }

    // Rows are already on screen, so the error is only about this one load. It goes to a
    // snackbar. The state keeps the old rows with no error.
    @Test
    fun `a failure with rows on screen goes to the effect`() = runTest {
        holdingRepository.saveHolding(createHolding(BITCOIN, "2"))
        coinRepository.marketCoins = mapOf(BITCOIN to createCoin(BITCOIN))
        val viewModel = viewModel()

        viewModel.effect.test {
            coinRepository.marketError = AppError.RateLimited
            viewModel.onIntent(PortfolioIntent.LoadPortfolio)

            assertThat(awaitItem()).isEqualTo(PortfolioEffect.ShowError(AppError.RateLimited))
        }

        val state = viewModel.state.value
        assertThat(state.error).isNull()
        assertThat(state.portfolio.valuedHoldings).hasSize(1)
    }

    @Test
    fun `a retry after a failure shows the rows`() = runTest {
        holdingRepository.saveHolding(createHolding(BITCOIN, "2"))
        holdingRepository.error = AppError.Storage
        val viewModel = viewModel()

        holdingRepository.error = null
        coinRepository.marketCoins = mapOf(BITCOIN to createCoin(BITCOIN))
        viewModel.onIntent(PortfolioIntent.LoadPortfolio)

        val state = viewModel.state.value
        assertThat(state.error).isNull()
        assertThat(state.portfolio.valuedHoldings).hasSize(1)
    }

    // The ViewModel keeps listening after the first load. A holding saved somewhere else shows up
    // here, with no reload.
    @Test
    fun `a new holding adds a row`() = runTest {
        coinRepository.marketCoins = mapOf(BITCOIN to createCoin(BITCOIN))
        val viewModel = viewModel()

        holdingRepository.saveHolding(createHolding(BITCOIN, "2"))

        assertThat(viewModel.state.value.portfolio.valuedHoldings).hasSize(1)
    }
}
