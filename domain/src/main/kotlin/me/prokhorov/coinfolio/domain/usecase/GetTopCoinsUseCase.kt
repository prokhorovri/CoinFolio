package me.prokhorov.coinfolio.domain.usecase

import javax.inject.Inject
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.repository.CoinRepository
import me.prokhorov.coinfolio.domain.result.Resource

class GetTopCoinsUseCase @Inject constructor(
    private val repository: CoinRepository,
) {
    suspend operator fun invoke(): Resource<List<Coin>> = repository.getTopCoins()
}
