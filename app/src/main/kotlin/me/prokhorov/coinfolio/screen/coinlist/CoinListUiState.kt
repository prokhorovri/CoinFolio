package me.prokhorov.coinfolio.screen.coinlist

import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.result.AppError

data class CoinListUiState(
    val isLoading: Boolean = false,
    val coins: List<Coin> = emptyList(),
    val error: AppError? = null,
)
