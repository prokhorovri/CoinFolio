package me.prokhorov.coinfolio.domain.usecase

import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository

class AddHoldingUseCase(
    private val repository: PortfolioRepository,
) {
    suspend operator fun invoke(holding: Holding) = repository.addHolding(holding)
}
