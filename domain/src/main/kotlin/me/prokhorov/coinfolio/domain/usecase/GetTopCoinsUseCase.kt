package me.prokhorov.coinfolio.domain.usecase

import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.Resource
import me.prokhorov.coinfolio.domain.repository.CoinRepository
import javax.inject.Inject

class GetTopCoinsUseCase @Inject constructor(
    private val repository: CoinRepository,
) {
    suspend operator fun invoke(): Resource<List<Coin>> = repository.getTopCoins()
}
