package me.prokhorov.coinfolio.domain.usecase

import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository
import javax.inject.Inject

/**
 * Stores the absolute quantity held of a coin, replacing any previous value.
 * A quantity of zero removes the holding — an empty position is not a position.
 */
class SaveHoldingUseCase @Inject constructor(
    private val repository: PortfolioRepository,
) {
    suspend operator fun invoke(holding: Holding) {
        if (holding.quantity.signum() == 0) {
            repository.deleteHolding(holding.coinId)
        } else {
            repository.saveHolding(holding)
        }
    }
}
