package me.prokhorov.coinfolio.screen.coinlist

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CompletableDeferred
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

    // The names and the symbols differ, so a test can tell which of the two matched.
    private val searchCoins = listOf(
        createCoin("bitcoin", symbol = "btc"),
        createCoin("ethereum", symbol = "eth"),
        createCoin("tether", symbol = "usdt"),
    )

    private fun filteredIds(viewModel: CoinListViewModel): List<String> {
        return viewModel.state.value.filteredCoins.map { it.id }
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

    // A pull keeps the rows visible. The full screen spinner belongs to the first load only.
    @Test
    fun `a refresh keeps the coins on screen while it runs`() {
        repository.topCoins = Resource.Success(listOf(createCoin("bitcoin")))
        val viewModel = viewModel()
        repository.gate = CompletableDeferred()

        viewModel.onIntent(CoinListIntent.RefreshCoins)

        val state = viewModel.state.value
        assertThat(state.isRefreshing).isTrue()
        assertThat(state.isLoading).isFalse()
        assertThat(state.coins.map { it.id }).containsExactly("bitcoin")
    }

    @Test
    fun `a refresh shows the new coins when it ends`() {
        repository.topCoins = Resource.Success(listOf(createCoin("bitcoin")))
        val viewModel = viewModel()

        repository.topCoins = Resource.Success(listOf(createCoin("ethereum")))
        viewModel.onIntent(CoinListIntent.RefreshCoins)

        val state = viewModel.state.value
        assertThat(state.isRefreshing).isFalse()
        assertThat(state.coins.map { it.id }).containsExactly("ethereum")
    }

    // Every request counts toward the rate limit, so a second pull must not send one more.
    @Test
    fun `a second refresh while one runs is dropped`() {
        repository.topCoins = Resource.Success(listOf(createCoin("bitcoin")))
        val viewModel = viewModel()
        val gate = CompletableDeferred<Unit>()
        repository.gate = gate

        viewModel.onIntent(CoinListIntent.RefreshCoins)
        repository.topCoins = Resource.Success(listOf(createCoin("ethereum")))
        viewModel.onIntent(CoinListIntent.RefreshCoins)
        gate.complete(Unit)

        // Without the guard the second call would have read ethereum.
        assertThat(viewModel.state.value.coins.map { it.id }).containsExactly("bitcoin")
    }

    @Test
    fun `a failure during a refresh keeps the coins and goes to the effect`() = runTest {
        repository.topCoins = Resource.Success(listOf(createCoin("bitcoin")))
        val viewModel = viewModel()

        viewModel.effect.test {
            repository.topCoins = Resource.Failure(AppError.RateLimited)
            viewModel.onIntent(CoinListIntent.RefreshCoins)

            assertThat(awaitItem()).isEqualTo(CoinListEffect.ShowError(AppError.RateLimited))
        }

        val state = viewModel.state.value
        assertThat(state.isRefreshing).isFalse()
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

    @Test
    fun `a query matches the coin name`() {
        repository.topCoins = Resource.Success(searchCoins)
        val viewModel = viewModel()

        viewModel.onIntent(CoinListIntent.ChangeQuery("ther"))

        assertThat(filteredIds(viewModel)).containsExactly("ethereum", "tether").inOrder()
    }

    @Test
    fun `a query matches the coin symbol`() {
        repository.topCoins = Resource.Success(searchCoins)
        val viewModel = viewModel()

        viewModel.onIntent(CoinListIntent.ChangeQuery("usdt"))

        assertThat(filteredIds(viewModel)).containsExactly("tether")
    }

    @Test
    fun `a query ignores letter case`() {
        repository.topCoins = Resource.Success(searchCoins)
        val viewModel = viewModel()

        viewModel.onIntent(CoinListIntent.ChangeQuery("BiTc"))

        assertThat(filteredIds(viewModel)).containsExactly("bitcoin")
    }

    @Test
    fun `a blank query shows every coin`() {
        repository.topCoins = Resource.Success(searchCoins)
        val viewModel = viewModel()

        viewModel.onIntent(CoinListIntent.ChangeQuery("   "))

        assertThat(filteredIds(viewModel))
            .containsExactly("bitcoin", "ethereum", "tether")
            .inOrder()
    }

    // The screen shows "not found" here, not the empty state, because the full list has coins.
    @Test
    fun `a query with no match shows no coins`() {
        repository.topCoins = Resource.Success(searchCoins)
        val viewModel = viewModel()

        viewModel.onIntent(CoinListIntent.ChangeQuery("xyz"))

        val state = viewModel.state.value
        assertThat(state.filteredCoins).isEmpty()
        assertThat(state.coins).hasSize(3)
    }

    @Test
    fun `a refresh filters the new coins by the current query`() {
        repository.topCoins = Resource.Success(listOf(createCoin("bitcoin", symbol = "btc")))
        val viewModel = viewModel()
        viewModel.onIntent(CoinListIntent.ChangeQuery("reum"))

        repository.topCoins = Resource.Success(searchCoins)
        viewModel.onIntent(CoinListIntent.RefreshCoins)

        assertThat(filteredIds(viewModel)).containsExactly("ethereum")
    }
}
