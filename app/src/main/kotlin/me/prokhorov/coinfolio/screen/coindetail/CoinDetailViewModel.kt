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
import me.prokhorov.coinfolio.util.formatAsQuantity
import javax.inject.Inject

/**
 * Limits how large a pasted number can be. `1E+999999999` is valid, but after formatting or
 * saving to Room it becomes a string that is one gigabyte long.
 */
private val QUANTITY_PATTERN = Regex("""\d{0,15}(\.\d{0,18})?""")

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val getCoinDetailUseCase: GetCoinDetailUseCase,
    private val getHoldingUseCase: GetHoldingUseCase,
    private val saveHoldingUseCase: SaveHoldingUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val coinId = savedStateHandle.toRoute<ScreenRoute.CoinDetail>().id

    private val _state = MutableStateFlow(CoinDetailUiState(coinId = coinId, isLoading = true))
    val state: StateFlow<CoinDetailUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CoinDetailEffect>()
    val effect: SharedFlow<CoinDetailEffect> = _effect.asSharedFlow()

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

    private fun loadCoin() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getCoinDetailUseCase(coinId)) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, coin = result.data)
                }

                is Resource.Failure -> {
                    _state.update { it.copy(isLoading = false, error = result.error) }
                    _effect.emit(CoinDetailEffect.ShowError(result.error))
                }
            }
        }
    }

    /**
     * A failure is the last value this flow emits, so the shown holding stops updating. The
     * error goes to state, not to an effect: at init nothing collects effects yet, so the
     * effect would be lost. A finished flow cannot start again, so calling this function once
     * more creates a new one. Do that only while [CoinDetailUiState.holdingError] is set,
     * because then the old collector has already finished.
     */
    private fun observeHolding() {
        viewModelScope.launch {
            getHoldingUseCase(coinId).collect { result ->
                when (result) {
                    is Resource.Success -> _state.update {
                        it.copy(savedQuantity = result.data?.quantity, holdingError = null)
                    }

                    is Resource.Failure -> _state.update {
                        it.copy(holdingError = result.error)
                    }
                }
            }
        }
    }

    /**
     * Rejected input is dropped instead of being shown, so the field can never hold a value
     * that [CoinDetailUiState.parsedQuantity] refuses. A comma becomes a dot. Otherwise `0,5`
     * from a decimal keyboard would block saving, and the user would not know why.
     */
    private fun updateQuantity(value: String) {
        val normalized = value.replace(',', '.')
        if (!normalized.matches(QUANTITY_PATTERN)) return
        _state.update { it.copy(editedQuantity = normalized) }
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
        val quantity = currentState.parsedQuantity ?: return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            val holding = Holding(coinId = coinId, quantity = quantity)
            when (val result = saveHoldingUseCase(holding)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(isSaving = false, editedQuantity = quantity.formatAsQuantity())
                    }
                    if (_state.value.holdingError != null) {
                        observeHolding()
                    }
                    _effect.emit(CoinDetailEffect.HoldingSaved)
                }

                is Resource.Failure -> {
                    _state.update { it.copy(isSaving = false) }
                    _effect.emit(CoinDetailEffect.ShowError(result.error))
                }
            }
        }
    }
}
