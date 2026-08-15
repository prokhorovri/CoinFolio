package me.prokhorov.coinfolio.screen.coindetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.usecase.GetCoinDetailUseCase
import me.prokhorov.coinfolio.domain.usecase.GetHoldingUseCase
import me.prokhorov.coinfolio.domain.usecase.SaveHoldingUseCase
import me.prokhorov.coinfolio.navigation.ScreenRoute
import me.prokhorov.coinfolio.util.LengthProblem
import me.prokhorov.coinfolio.util.QuantityInput
import me.prokhorov.coinfolio.util.QuantityInputResult
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val getCoinDetailUseCase: GetCoinDetailUseCase,
    private val getHoldingUseCase: GetHoldingUseCase,
    private val saveHoldingUseCase: SaveHoldingUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val coinId = savedStateHandle.toRoute<ScreenRoute.CoinDetail>().id

    private val _state = MutableStateFlow(CoinDetailUiState(coinId = coinId))
    val state: StateFlow<CoinDetailUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CoinDetailEffect>()
    val effect: SharedFlow<CoinDetailEffect> = _effect.asSharedFlow()

    /**
     * True after the first keystroke. Until then the field follows the database, and an untouched
     * field is never marked as wrong. The screen never draws it, so it stays out of the state.
     *
     * Only the main thread touches it, so a plain field is safe: `viewModelScope` runs on
     * `Dispatchers.Main.immediate`, and a flow collects on the thread of the collector. Do not
     * start a coroutine here with another dispatcher.
     */
    private var isQuantityEdited = false

    init {
        onIntent(CoinDetailIntent.LoadCoin)
        observeHolding()
    }

    fun onIntent(intent: CoinDetailIntent) {
        when (intent) {
            is CoinDetailIntent.LoadCoin -> loadCoin()
            is CoinDetailIntent.QuantityChanged -> updateQuantity(intent.value)
            is CoinDetailIntent.SaveHolding -> saveHolding()
        }
    }

    /**
     * One error goes to one place. While the coin is on screen the error is temporary, so it goes
     * to a snackbar. While the screen is still empty the error is the whole screen, so it goes to
     * the state and a snackbar would only repeat it.
     */
    private fun loadCoin() {
        viewModelScope.launch {
            updateState { it.copy(error = null) }
            when (val result = getCoinDetailUseCase(coinId)) {
                is Resource.Success -> updateState { it.copy(coin = result.data) }

                is Resource.Failure -> {
                    val hasData = _state.value.coin != null
                    updateState { it.copy(error = if (hasData) null else result.error) }
                    if (hasData) {
                        _effect.emit(CoinDetailEffect.ShowError(result.error))
                    }
                }
            }
        }
    }

    /**
     * A failure is the last value of this flow, so the holding stops updating. The error goes to
     * the state and not to an effect, because nothing collects effects at init yet.
     *
     * A finished flow cannot start again. Calling this function makes a new one, so call it only
     * while [CoinDetailUiState.holdingError] is set, when the old collector is already done.
     *
     * The stored value also fills the field, but only while the user has typed nothing. After the
     * first keystroke the field belongs to the user, even if the database answers later.
     */
    private fun observeHolding() {
        viewModelScope.launch {
            getHoldingUseCase(coinId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        val quantity = result.data?.quantity
                        updateState {
                            it.copy(
                                savedQuantity = quantity,
                                quantityInput = if (isQuantityEdited) {
                                    it.quantityInput
                                } else {
                                    quantity?.let(QuantityInput::format).orEmpty()
                                },
                                holdingError = null,
                            )
                        }
                    }

                    is Resource.Failure -> updateState { it.copy(holdingError = result.error) }
                }
            }
        }
    }

    /**
     * The text is stored as it arrives, always. Dropping it here would split the truth in two:
     * the field would show the new text while the state kept the old one, and nothing would tell
     * the user why the number cannot be saved.
     *
     * [derived] then says what is wrong with it and whether it can be saved.
     */
    private fun updateQuantity(value: String) {
        isQuantityEdited = true
        updateState { it.copy(quantityInput = value) }
    }

    /**
     * The field is set from the value we just wrote, not from the database. The database sends
     * the same value back later, and by then it would replace anything typed in the meantime.
     *
     * If the write succeeds while [observeHolding] is dead, the database accepts queries again,
     * so it is worth reading once more. A stored quantity in the wrong format breaks every read
     * while writes still work, and saving a new value replaces that row. A full disk breaks the
     * writing too, so this code never runs in that case.
     *
     * A save already in flight blocks a new one. The button is disabled while it runs, but an
     * intent can also arrive from somewhere else, so the guard belongs here too.
     */
    private fun saveHolding() {
        val currentState = _state.value
        if (currentState.isSaving) return

        val quantity = QuantityInput.normalize(currentState.quantityInput).toQuantity() ?: return

        updateState { it.copy(isSaving = true) }
        viewModelScope.launch {
            val holding = Holding(coinId = coinId, quantity = quantity)
            when (val result = saveHoldingUseCase(holding)) {
                is Resource.Success -> {
                    updateState {
                        it.copy(isSaving = false, quantityInput = QuantityInput.format(quantity))
                    }
                    if (_state.value.holdingError != null) {
                        observeHolding()
                    }
                    _effect.emit(CoinDetailEffect.HoldingSaved)
                }

                is Resource.Failure -> {
                    updateState { it.copy(isSaving = false) }
                    _effect.emit(CoinDetailEffect.ShowError(result.error))
                }
            }
        }
    }

    /**
     * The only way to change the state. It runs [derived] after every change, so the fields that
     * follow from the others can never fall behind. Without it each caller would have to remember
     * which of them its change affects.
     */
    private fun updateState(transform: (CoinDetailUiState) -> CoinDetailUiState) {
        _state.update { transform(it).derived() }
    }

    /** Fills the fields that follow from the others. */
    private fun CoinDetailUiState.derived(): CoinDetailUiState {
        val result = QuantityInput.normalize(quantityInput)
        return copy(
            quantityError = if (isQuantityEdited) result.toQuantityError() else null,
            canSave = coin != null && !isSaving && result.toQuantity() != null,
        )
    }
}

/** Blocks saving if the text is empty or not a number. `0` is allowed: it clears the holding. */
private fun QuantityInputResult.toQuantity(): BigDecimal? {
    return (this as? QuantityInputResult.Valid)
        ?.text
        ?.takeIf(String::isNotEmpty)
        ?.let(QuantityInput::toCanonical)
        ?.toBigDecimalOrNull()
        ?.takeIf { it.signum() >= 0 }
}

private fun QuantityInputResult.toQuantityError(): QuantityError? {
    return when (this) {
        is QuantityInputResult.Valid -> null
        is QuantityInputResult.InvalidCharacter -> QuantityError.INVALID_CHARACTER
        is QuantityInputResult.TooLong -> when (problem) {
            LengthProblem.INTEGER_DIGITS -> QuantityError.TOO_MANY_INTEGER_DIGITS
            LengthProblem.FRACTION_DIGITS -> QuantityError.TOO_MANY_FRACTION_DIGITS
        }
    }
}
