package me.prokhorov.coinfolio.screen.coinlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.usecase.GetTopCoinsUseCase
import javax.inject.Inject

@HiltViewModel
class CoinListViewModel @Inject constructor(
    private val getTopCoinsUseCase: GetTopCoinsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListUiState(isLoading = true))
    val state: StateFlow<CoinListUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CoinListEffect>()
    val effect: SharedFlow<CoinListEffect> = _effect.asSharedFlow()

    init {
        onIntent(CoinListIntent.LoadCoins)
    }

    fun onIntent(intent: CoinListIntent) {
        when (intent) {
            is CoinListIntent.LoadCoins -> loadCoins()
        }
    }

    /**
     * One error goes to one place. While coins are on screen the error is temporary, so it goes to
     * a snackbar. While the list is still empty the error is the whole screen, so it goes to the
     * state and a snackbar would only repeat it.
     */
    private fun loadCoins() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = getTopCoinsUseCase()) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, coins = result.data)
                }

                is Resource.Failure -> {
                    val hasData = _state.value.coins.isNotEmpty()
                    _state.update {
                        it.copy(isLoading = false, error = if (hasData) null else result.error)
                    }
                    if (hasData) {
                        _effect.emit(CoinListEffect.ShowError(result.error))
                    }
                }
            }
        }
    }
}
