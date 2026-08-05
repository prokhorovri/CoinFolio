package me.prokhorov.coinfolio.screen.coindetail

import me.prokhorov.coinfolio.domain.result.AppError

sealed interface CoinDetailIntent {
    data object LoadCoin : CoinDetailIntent
    data class QuantityChanged(val value: String) : CoinDetailIntent
    data object SaveHolding : CoinDetailIntent
}

sealed interface CoinDetailEffect {
    data class ShowError(val error: AppError) : CoinDetailEffect
    data object HoldingSaved : CoinDetailEffect
}
