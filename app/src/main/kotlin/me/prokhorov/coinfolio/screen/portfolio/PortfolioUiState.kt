package me.prokhorov.coinfolio.screen.portfolio

import me.prokhorov.coinfolio.domain.model.Portfolio
import me.prokhorov.coinfolio.domain.result.AppError

data class PortfolioUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val portfolio: Portfolio = Portfolio(emptyList()),
    val error: AppError? = null,
)
