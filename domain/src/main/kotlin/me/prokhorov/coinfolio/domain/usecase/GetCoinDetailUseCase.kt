package me.prokhorov.coinfolio.domain.usecase

import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.repository.CoinRepository
import me.prokhorov.coinfolio.domain.result.Resource
import javax.inject.Inject

class GetCoinDetailUseCase @Inject constructor(
    private val repository: CoinRepository,
) {
    suspend operator fun invoke(coinId: String): Resource<CoinDetail> {
        return repository.getCoinDetail(coinId)
    }
}
