package me.prokhorov.coinfolio.data.repository

import javax.inject.Inject
import me.prokhorov.coinfolio.data.mapper.toDomain
import me.prokhorov.coinfolio.data.remote.CoinRemoteDataSource
import me.prokhorov.coinfolio.data.remote.dto.CoinDto
import me.prokhorov.coinfolio.data.remote.util.apiCall
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.repository.CoinRepository
import me.prokhorov.coinfolio.domain.result.Resource

class CoinRepositoryImpl @Inject constructor(
    private val remoteDataSource: CoinRemoteDataSource,
) : CoinRepository {
    override suspend fun getTopCoins(): Resource<List<Coin>> {
        return apiCall { remoteDataSource.getTopCoins().map(CoinDto::toDomain) }
    }

    override suspend fun getCoinDetail(coinId: String): Resource<CoinDetail> {
        return apiCall { remoteDataSource.getCoinDetail(coinId).toDomain() }
    }
}
