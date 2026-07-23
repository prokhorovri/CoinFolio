package me.prokhorov.coinfolio.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository

class GetHoldingsUseCase(
    private val repository: PortfolioRepository,
) {
    operator fun invoke(): Flow<List<Holding>> = repository.getHoldings()
}
