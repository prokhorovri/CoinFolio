package me.prokhorov.coinfolio.screen.coindetail

sealed interface CoinDetailIntent {
    data object LoadCoin : CoinDetailIntent
    data class QuantityChanged(val value: String) : CoinDetailIntent
    data object SaveHolding : CoinDetailIntent
}

sealed interface CoinDetailEffect {
    data class ShowError(val message: String) : CoinDetailEffect
    data object HoldingSaved : CoinDetailEffect
}
