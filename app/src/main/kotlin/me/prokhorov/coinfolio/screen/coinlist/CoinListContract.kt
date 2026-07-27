package me.prokhorov.coinfolio.screen.coinlist

sealed interface CoinListIntent {
    data object LoadCoins : CoinListIntent
}

sealed interface CoinListEffect {
    data class ShowError(val message: String) : CoinListEffect
}
