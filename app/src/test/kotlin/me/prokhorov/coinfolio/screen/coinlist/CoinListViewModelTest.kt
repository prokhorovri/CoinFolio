package me.prokhorov.coinfolio.screen.coinlist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.createCoin
import me.prokhorov.coinfolio.domain.usecase.GetTopCoinsUseCase
import me.prokhorov.coinfolio.testing.MainDispatcherRule
import org.junit.Rule
import org.junit.Test

class CoinListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeCoinRepository()

    private fun viewModel(): CoinListViewModel {
        return CoinListViewModel(GetTopCoinsUseCase(repository))
    }

    @Test
    fun `loads the coins on start`() {
        repository.topCoins = Resource.Success(
            listOf(
                createCoin("bitcoin"),
                createCoin("ethereum"),
            ),
        )

        val state = viewModel().state.value

        assertThat(state.coins.map { it.id }).containsExactly("bitcoin", "ethereum").inOrder()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `an empty list is a success, not an error`() {
        repository.topCoins = Resource.Success(emptyList())

        val state = viewModel().state.value

        assertThat(state.coins).isEmpty()
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    // The screen has nothing to show yet, so the error fills the whole screen. It belongs in the
    // state. A snackbar would only say the same thing again.
    @Test
    fun `a failure with no coins goes to the state`() {
        repository.topCoins = Resource.Failure(AppError.Network)

        val state = viewModel().state.value

        assertThat(state.error).isEqualTo(AppError.Network)
        assertThat(state.isLoading).isFalse()
    }

    // Coins are already on screen, so the error is only about this one load. It goes to a
    // snackbar. The state keeps the old list with no error.
    @Test
    fun `a failure with coins on screen goes to the effect`() = runTest {
        repository.topCoins = Resource.Success(listOf(createCoin("bitcoin")))
        val viewModel = viewModel()

        viewModel.effect.test {
            repository.topCoins = Resource.Failure(AppError.RateLimited)
            viewModel.onIntent(CoinListIntent.LoadCoins)

            assertThat(awaitItem()).isEqualTo(CoinListEffect.ShowError(AppError.RateLimited))
        }

        val state = viewModel.state.value
        assertThat(state.error).isNull()
        assertThat(state.coins.map { it.id }).containsExactly("bitcoin")
    }

    @Test
    fun `a retry after a failure shows the coins`() {
        repository.topCoins = Resource.Failure(AppError.Network)
        val viewModel = viewModel()

        repository.topCoins = Resource.Success(listOf(createCoin("bitcoin")))
        viewModel.onIntent(CoinListIntent.LoadCoins)

        val state = viewModel.state.value
        assertThat(state.error).isNull()
        assertThat(state.coins.map { it.id }).containsExactly("bitcoin")
    }
}
