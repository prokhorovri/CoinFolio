package me.prokhorov.coinfolio.screen.coindetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import java.util.Locale
import kotlinx.coroutines.test.runTest
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.testing.FakeCoinRepository
import me.prokhorov.coinfolio.domain.testing.FakePortfolioRepository
import me.prokhorov.coinfolio.domain.testing.createCoinDetail
import me.prokhorov.coinfolio.domain.testing.createHolding
import me.prokhorov.coinfolio.domain.usecase.GetCoinDetailUseCase
import me.prokhorov.coinfolio.domain.usecase.GetHoldingUseCase
import me.prokhorov.coinfolio.domain.usecase.SaveHoldingUseCase
import me.prokhorov.coinfolio.testing.MainDispatcherRule
import me.prokhorov.coinfolio.util.DefaultLocaleRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private const val COIN_ID = "bitcoin"

/**
 * Runs on Robolectric, unlike the other tests here. The ViewModel reads its coin id with
 * `SavedStateHandle.toRoute`, and that builds an `android.os.Bundle`.
 */
@RunWith(RobolectricTestRunner::class)
class CoinDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // The field shows the decimal separator of the locale, so the machine that runs the build
    // must not decide what the text looks like.
    @get:Rule
    val localeRule = DefaultLocaleRule(Locale.US)

    private val coinRepository = FakeCoinRepository()
    private val portfolioRepository = FakePortfolioRepository()

    private fun viewModel(): CoinDetailViewModel {
        return CoinDetailViewModel(
            getCoinDetailUseCase = GetCoinDetailUseCase(coinRepository),
            getHoldingUseCase = GetHoldingUseCase(portfolioRepository),
            saveHoldingUseCase = SaveHoldingUseCase(portfolioRepository),
            savedStateHandle = SavedStateHandle(mapOf("id" to COIN_ID)),
        )
    }

    private fun makeCoinAvailable() {
        coinRepository.coinDetails = mapOf(COIN_ID to Resource.Success(createCoinDetail(COIN_ID)))
    }

    // Loading the coin

    @Test
    fun `loads the coin on start`() {
        makeCoinAvailable()

        val state = viewModel().state.value

        assertThat(state.coin?.id).isEqualTo(COIN_ID)
        assertThat(state.error).isNull()
    }

    // Nothing is on screen yet, so the error fills the whole screen and belongs in the state.
    @Test
    fun `a failure with no coin goes to the state`() {
        coinRepository.coinDetails = mapOf(COIN_ID to Resource.Failure(AppError.NotFound))

        val state = viewModel().state.value

        assertThat(state.error).isEqualTo(AppError.NotFound)
        assertThat(state.coin).isNull()
    }

    // The coin is already on screen, so this error is only about the second load.
    @Test
    fun `a failure with the coin on screen goes to the effect`() = runTest {
        makeCoinAvailable()
        val viewModel = viewModel()

        viewModel.effect.test {
            coinRepository.coinDetails = mapOf(COIN_ID to Resource.Failure(AppError.RateLimited))
            viewModel.onIntent(CoinDetailIntent.LoadCoin)

            assertThat(awaitItem()).isEqualTo(CoinDetailEffect.ShowError(AppError.RateLimited))
        }

        assertThat(viewModel.state.value.error).isNull()
        assertThat(viewModel.state.value.coin?.id).isEqualTo(COIN_ID)
    }

    // The quantity field

    @Test
    fun `the stored holding fills the field`() = runTest {
        makeCoinAvailable()
        portfolioRepository.saveHolding(createHolding(COIN_ID, "2.5"))

        val state = viewModel().state.value

        assertThat(state.quantityInput).isEqualTo("2.5")
        assertThat(state.savedQuantity).isEqualTo(BigDecimal("2.5"))
    }

    @Test
    fun `a coin that is not held leaves the field empty`() {
        makeCoinAvailable()

        val state = viewModel().state.value

        assertThat(state.quantityInput).isEmpty()
        assertThat(state.savedQuantity).isNull()
    }

    // The field belongs to the user after the first keystroke. The database answers later, and
    // without this rule its value would replace the text while it is being typed.
    @Test
    fun `the database does not replace text the user typed`() = runTest {
        makeCoinAvailable()
        val viewModel = viewModel()

        viewModel.onIntent(CoinDetailIntent.QuantityChanged("7"))
        portfolioRepository.saveHolding(createHolding(COIN_ID, "2.5"))

        val state = viewModel.state.value
        assertThat(state.quantityInput).isEqualTo("7")
        assertThat(state.savedQuantity).isEqualTo(BigDecimal("2.5"))
    }

    @Test
    fun `an untouched empty field is not an error`() {
        makeCoinAvailable()

        val state = viewModel().state.value

        assertThat(state.quantityError).isNull()
        assertThat(state.canSave).isFalse()
    }

    @Test
    fun `a letter in the field is reported`() {
        makeCoinAvailable()
        val viewModel = viewModel()

        viewModel.onIntent(CoinDetailIntent.QuantityChanged("1a"))

        val state = viewModel.state.value
        assertThat(state.quantityError).isEqualTo(QuantityError.INVALID_CHARACTER)
        assertThat(state.canSave).isFalse()
    }

    @Test
    fun `too many fraction digits are reported`() {
        makeCoinAvailable()
        val viewModel = viewModel()

        viewModel.onIntent(CoinDetailIntent.QuantityChanged("1." + "1".repeat(19)))

        val state = viewModel.state.value
        assertThat(state.quantityError).isEqualTo(QuantityError.TOO_MANY_FRACTION_DIGITS)
        assertThat(state.canSave).isFalse()
    }

    @Test
    fun `a valid number can be saved`() {
        makeCoinAvailable()
        val viewModel = viewModel()

        viewModel.onIntent(CoinDetailIntent.QuantityChanged("1.5"))

        val state = viewModel.state.value
        assertThat(state.quantityError).isNull()
        assertThat(state.canSave).isTrue()
    }

    // Without a coin the screen shows the error state, so there is nothing to save into.
    @Test
    fun `a number cannot be saved while the coin is missing`() {
        coinRepository.coinDetails = mapOf(COIN_ID to Resource.Failure(AppError.Network))
        val viewModel = viewModel()

        viewModel.onIntent(CoinDetailIntent.QuantityChanged("1.5"))

        assertThat(viewModel.state.value.canSave).isFalse()
    }

    // Saving

    @Test
    fun `a save stores the quantity`() = runTest {
        makeCoinAvailable()
        val viewModel = viewModel()
        viewModel.onIntent(CoinDetailIntent.QuantityChanged("1.5"))

        viewModel.effect.test {
            viewModel.onIntent(CoinDetailIntent.SaveHolding)

            assertThat(awaitItem()).isEqualTo(CoinDetailEffect.HoldingSaved)
        }

        val state = viewModel.state.value
        assertThat(state.savedQuantity).isEqualTo(BigDecimal("1.5"))
        assertThat(state.isSaving).isFalse()
    }

    // Zero is a valid quantity, and the use case turns it into a delete.
    @Test
    fun `saving zero clears the holding`() = runTest {
        makeCoinAvailable()
        portfolioRepository.saveHolding(createHolding(COIN_ID, "2.5"))
        val viewModel = viewModel()

        viewModel.onIntent(CoinDetailIntent.QuantityChanged("0"))
        viewModel.onIntent(CoinDetailIntent.SaveHolding)

        assertThat(viewModel.state.value.savedQuantity).isNull()
    }

    @Test
    fun `a failed save goes to the effect`() = runTest {
        makeCoinAvailable()
        val viewModel = viewModel()
        viewModel.onIntent(CoinDetailIntent.QuantityChanged("1.5"))
        portfolioRepository.error = AppError.Storage

        viewModel.effect.test {
            viewModel.onIntent(CoinDetailIntent.SaveHolding)

            assertThat(awaitItem()).isEqualTo(CoinDetailEffect.ShowError(AppError.Storage))
        }

        assertThat(viewModel.state.value.isSaving).isFalse()
    }

    // Reading the holding

    @Test
    fun `a read failure goes to holdingError`() {
        makeCoinAvailable()
        portfolioRepository.error = AppError.Storage

        val state = viewModel().state.value

        assertThat(state.holdingError).isEqualTo(AppError.Storage)
        // The read error belongs to the holding section only. The coin itself is on screen.
        assertThat(state.error).isNull()
        assertThat(state.coin?.id).isEqualTo(COIN_ID)
    }

    // A failed read ends the flow, so nothing is listening to the database any more. A save that
    // works proves the database answers again, so the ViewModel has to subscribe a second time.
    @Test
    fun `a save after a read failure starts reading again`() {
        makeCoinAvailable()
        portfolioRepository.error = AppError.Storage
        val viewModel = viewModel()
        assertThat(viewModel.state.value.holdingError).isEqualTo(AppError.Storage)

        portfolioRepository.error = null
        viewModel.onIntent(CoinDetailIntent.QuantityChanged("1.5"))
        viewModel.onIntent(CoinDetailIntent.SaveHolding)

        val state = viewModel.state.value
        assertThat(state.holdingError).isNull()
        assertThat(state.savedQuantity).isEqualTo(BigDecimal("1.5"))
    }
}
