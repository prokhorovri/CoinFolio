package me.prokhorov.coinfolio.screen.coinlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.usecase.GetTopCoinsUseCase

@HiltViewModel
class CoinListViewModel @Inject constructor(
    private val getTopCoinsUseCase: GetTopCoinsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListUiState(isLoading = true))
    val state: StateFlow<CoinListUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<CoinListEffect>()
    val effect: SharedFlow<CoinListEffect> = _effect.asSharedFlow()

    private var loadJob: Job? = null

    init {
        onIntent(CoinListIntent.LoadCoins)
    }

    fun onIntent(intent: CoinListIntent) {
        when (intent) {
            is CoinListIntent.LoadCoins -> loadCoins(isRefreshing = false)
            is CoinListIntent.RefreshCoins -> loadCoins(isRefreshing = true)
            is CoinListIntent.ChangeQuery -> changeQuery(intent.query)
        }
    }

    private fun changeQuery(query: String) {
        _state.update {
            it.copy(query = query, filteredCoins = filterCoins(it.coins, query))
        }
    }

    /**
     * While coins are on screen the error is temporary, so it goes to a snackbar. While the
     * list is still empty the error fills the whole screen. It goes to the state, and a
     * snackbar would only repeat it.
     */
    private fun loadCoins(isRefreshing: Boolean) {
        // Every request counts toward the rate limit, so drop a second call while one runs.
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update {
                it.copy(isLoading = !isRefreshing, isRefreshing = isRefreshing, error = null)
            }
            when (val result = getTopCoinsUseCase()) {
                is Resource.Success -> _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        coins = result.data,
                        filteredCoins = filterCoins(result.data, it.query),
                    )
                }

                is Resource.Failure -> {
                    val hasData = _state.value.coins.isNotEmpty()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = if (hasData) null else result.error,
                        )
                    }
                    if (hasData) {
                        _effect.emit(CoinListEffect.ShowError(result.error))
                    }
                }
            }
        }
    }

    private fun filterCoins(coins: List<Coin>, query: String): List<Coin> {
        val text = query.trim()
        if (text.isEmpty()) return coins

        return coins.filter { coin ->
            val name = coin.name
            val symbol = coin.symbol

            name.contains(text, ignoreCase = true) || symbol.contains(text, ignoreCase = true)
        }
    }
}
