package me.prokhorov.coinfolio.data.repository

import me.prokhorov.coinfolio.data.mapper.toDomain
import me.prokhorov.coinfolio.data.remote.CoinRemoteDataSource
import me.prokhorov.coinfolio.data.remote.dto.CoinDto
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.Resource
import me.prokhorov.coinfolio.domain.repository.CoinRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CoinRepositoryImpl @Inject constructor(
    private val remoteDataSource: CoinRemoteDataSource,
) : CoinRepository {
    override suspend fun getTopCoins(): Resource<List<Coin>> {
        return try {
            Resource.Success(remoteDataSource.getTopCoins().map(CoinDto::toDomain))
        } catch (e: IOException) {
            Resource.Error(e.message ?: "Network error")
        } catch (e: HttpException) {
            Resource.Error(e.message ?: "Server error")
        }
    }
}
