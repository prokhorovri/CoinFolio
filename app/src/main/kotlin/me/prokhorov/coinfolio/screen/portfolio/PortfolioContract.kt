package me.prokhorov.coinfolio.screen.portfolio

import me.prokhorov.coinfolio.domain.result.AppError

sealed interface PortfolioIntent {
    data object LoadPortfolio : PortfolioIntent
}

sealed interface PortfolioEffect {
    data class ShowError(val error: AppError) : PortfolioEffect
}
