package me.prokhorov.coinfolio.screen.coinlist

import me.prokhorov.coinfolio.domain.model.Coin

data class CoinListUiState(
    val isLoading: Boolean = false,
    val coins: List<Coin> = emptyList(),
    val errorMessage: String? = null,
)
