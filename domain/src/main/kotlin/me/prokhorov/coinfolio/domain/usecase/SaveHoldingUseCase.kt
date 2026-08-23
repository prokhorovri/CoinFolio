package me.prokhorov.coinfolio.domain.usecase

import javax.inject.Inject
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository
import me.prokhorov.coinfolio.domain.result.Resource

/**
 * Stores the full quantity of a coin and replaces the previous value.
 * A quantity of zero deletes the holding instead of storing a row with zero.
 */
class SaveHoldingUseCase @Inject constructor(
    private val repository: PortfolioRepository,
) {
    suspend operator fun invoke(holding: Holding): Resource<Unit> {
        return if (holding.quantity.signum() == 0) {
            repository.deleteHolding(holding.coinId)
        } else {
            repository.saveHolding(holding)
        }
    }
}
