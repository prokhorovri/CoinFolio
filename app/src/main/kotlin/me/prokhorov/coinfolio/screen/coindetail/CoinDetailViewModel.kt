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
     * A read failure goes to state, not to an effect. An effect emitted from here is dropped:
     * [_effect] has no buffer, and at init the screen has not subscribed yet. State also keeps the
     * message on screen, which matches the failure being permanent, since the query flow stops
     * emitting once it fails.
     */
    private fun observeHolding() {
        viewModelScope.launch {
            getHoldingUseCase(coinId).collect { result ->
                when (result) {
                    is Resource.Success -> _state.update {
                        it.copy(savedQuantity = result.data?.quantity)
                    }

                    is Resource.Failure -> _state.update {
                        it.copy(holdingError = result.error)
                    }
                }
            }
        }
    }

    /**
     * Drops rejected input instead of showing it, so the field can never hold something
     * [CoinDetailUiState.parsedQuantity] would refuse. Commas become dots: a decimal keyboard
     * shows the locale separator, and `0,5` would otherwise just disable saving with no hint why.
     */
    private fun updateQuantity(value: String) {
        val normalized = value.replace(',', '.')
        if (!normalized.matches(QUANTITY_PATTERN)) return
        _state.update { it.copy(editedQuantity = normalized) }
    }

    /**
     * Re-syncs the field from the value just written, not from the stored holding echoing back.
     * That echo arrives after the write and would discard anything typed in between.
     */
    private fun saveHolding() {
        val quantity = _state.value.parsedQuantity ?: return
        viewModelScope.launch {
            val holding = Holding(coinId = coinId, quantity = quantity)
            when (val result = saveHoldingUseCase(holding)) {
                is Resource.Success -> {
                    _state.update { it.copy(editedQuantity = quantity.formatAsQuantity()) }
                    _effect.emit(CoinDetailEffect.HoldingSaved)
                }

                is Resource.Failure -> _effect.emit(CoinDetailEffect.ShowError(result.error))
            }
        }
    }
}
