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
import me.prokhorov.coinfolio.domain.model.Resource
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

    private fun loadCoins() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = getTopCoinsUseCase()) {
                is Resource.Success -> _state.update {
                    it.copy(isLoading = false, coins = result.data)
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                    _effect.emit(CoinListEffect.ShowError(result.message))
                }
                Resource.Loading -> Unit
            }
        }
    }
}
