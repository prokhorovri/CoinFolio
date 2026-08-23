package me.prokhorov.coinfolio.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository
import me.prokhorov.coinfolio.domain.result.Resource

class GetHoldingsUseCase @Inject constructor(
    private val repository: PortfolioRepository,
) {
    operator fun invoke(): Flow<Resource<List<Holding>>> {
        return repository.getHoldings()
    }
}
