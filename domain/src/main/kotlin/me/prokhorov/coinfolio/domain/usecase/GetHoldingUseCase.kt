package me.prokhorov.coinfolio.domain.usecase

import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.HoldingRepository
import me.prokhorov.coinfolio.domain.result.Resource

class GetHoldingUseCase @Inject constructor(
    private val repository: HoldingRepository,
) {
    operator fun invoke(coinId: String): Flow<Resource<Holding?>> {
        return repository.getHolding(coinId)
    }
}
