package me.prokhorov.coinfolio.screen.portfolio

import me.prokhorov.coinfolio.domain.model.Portfolio
import me.prokhorov.coinfolio.domain.result.AppError

/** Keeps the whole [Portfolio], so the total on screen always matches the rows above it. */
data class PortfolioUiState(
    val isLoading: Boolean = false,
    val portfolio: Portfolio = Portfolio(emptyList()),
    val error: AppError? = null,
)
