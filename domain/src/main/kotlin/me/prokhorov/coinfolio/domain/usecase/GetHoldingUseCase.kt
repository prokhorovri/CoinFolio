package me.prokhorov.coinfolio.domain.usecase

import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository
import javax.inject.Inject

class GetHoldingUseCase @Inject constructor(
    private val repository: PortfolioRepository,
) {
    operator fun invoke(coinId: String): Flow<Holding?> = repository.getHolding(coinId)
}
