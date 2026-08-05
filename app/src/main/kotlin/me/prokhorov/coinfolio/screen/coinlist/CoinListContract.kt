package me.prokhorov.coinfolio.screen.coinlist

import me.prokhorov.coinfolio.domain.result.AppError

sealed interface CoinListIntent {
    data object LoadCoins : CoinListIntent
}

sealed interface CoinListEffect {
    data class ShowError(val error: AppError) : CoinListEffect
}
