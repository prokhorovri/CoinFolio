package me.prokhorov.coinfolio.screen.portfolio

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
import me.prokhorov.coinfolio.domain.model.Portfolio
import me.prokhorov.coinfolio.domain.result.Resource
import me.prokhorov.coinfolio.domain.usecase.GetPortfolioUseCase

@HiltViewModel
class PortfolioViewModel @Inject constructor(
    private val getPortfolioUseCase: GetPortfolioUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(PortfolioUiState(isLoading = true))
    val state: StateFlow<PortfolioUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<PortfolioEffect>()
    val effect: SharedFlow<PortfolioEffect> = _effect.asSharedFlow()

    private var loadJob: Job? = null

    init {
        onIntent(PortfolioIntent.LoadPortfolio)
    }

    fun onIntent(intent: PortfolioIntent) {
        when (intent) {
            is PortfolioIntent.LoadPortfolio -> loadPortfolio()
        }
    }

    /**
     * A read failure ends the flow, so a retry needs a new collector. The old job is cancelled
     * first. A market failure does not end the flow, so two collectors could write the state.
     */
    private fun loadPortfolio() {
        loadJob?.cancel()
        _state.update { it.copy(isLoading = true, error = null) }
        loadJob = viewModelScope.launch {
            getPortfolioUseCase().collect(::reduce)
        }
    }

    /**
     * While rows are on screen the error goes to a snackbar. While the list is still empty it
     * goes to the state and fills the screen. A snackbar would then only repeat it.
     */
    private suspend fun reduce(result: Resource<Portfolio>) {
        when (result) {
            is Resource.Success -> _state.update {
                it.copy(isLoading = false, portfolio = result.data, error = null)
            }

            is Resource.Failure -> {
                val hasRows = _state.value.portfolio.valuedHoldings.isNotEmpty()
                _state.update {
                    it.copy(isLoading = false, error = if (hasRows) null else result.error)
                }
                if (hasRows) {
                    _effect.emit(PortfolioEffect.ShowError(result.error))
                }
            }
        }
    }
}
