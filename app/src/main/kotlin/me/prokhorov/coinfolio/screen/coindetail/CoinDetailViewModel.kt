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
 * Caps how large a pasted number can get. `1E+999999999` parses fine, but expands to a
 * gigabyte-long string once it is formatted or written to Room.
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
     * A failure is the last thing this flow emits, so the shown holding freezes. That goes to
     * state, not to an effect: at init the screen has not subscribed, and an unbuffered effect
     * would be dropped. Calling this again re-subscribes, since a finished flow cannot resume.
     * Only do that while [CoinDetailUiState.holdingError] is set, or two collectors could run.
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
     * Drops rejected input, so the field cannot hold what [CoinDetailUiState.parsedQuantity]
     * refuses. Commas become dots, or `0,5` from a decimal keyboard would silently block saving.
     */
    private fun updateQuantity(value: String) {
        val normalized = value.replace(',', '.')
        if (!normalized.matches(QUANTITY_PATTERN)) return
        _state.update { it.copy(editedQuantity = normalized) }
    }

    /**
     * Re-syncs the field from the value just written, because the database echo lands later and
     * would wipe out anything typed in between.
     *
     * A write landing while [observeHolding] is dead means the database takes queries again, so a
     * re-read is worth it: an unparseable stored quantity breaks every read while writes still
     * work, and saving over it fixes the row. A full disk fails the write too, so this never runs.
     */
    private fun saveHolding() {
        val quantity = _state.value.parsedQuantity ?: return
        viewModelScope.launch {
            val holding = Holding(coinId = coinId, quantity = quantity)
            when (val result = saveHoldingUseCase(holding)) {
                is Resource.Success -> {
                    _state.update { it.copy(editedQuantity = quantity.formatAsQuantity()) }
                    if (_state.value.holdingError != null) {
                        observeHolding()
                    }
                    _effect.emit(CoinDetailEffect.HoldingSaved)
                }

                is Resource.Failure -> _effect.emit(CoinDetailEffect.ShowError(result.error))
            }
        }
    }
}
