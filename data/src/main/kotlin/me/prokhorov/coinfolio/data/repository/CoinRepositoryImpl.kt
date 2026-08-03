package me.prokhorov.coinfolio.data.repository

import me.prokhorov.coinfolio.data.mapper.toDomain
import me.prokhorov.coinfolio.data.remote.CoinRemoteDataSource
import me.prokhorov.coinfolio.data.remote.dto.CoinDto
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.repository.CoinRepository
import me.prokhorov.coinfolio.domain.result.Resource
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection
import javax.inject.Inject

class CoinRepositoryImpl @Inject constructor(
    private val remoteDataSource: CoinRemoteDataSource,
) : CoinRepository {
    override suspend fun getTopCoins(): Resource<List<Coin>> {
        return try {
            Resource.Success(remoteDataSource.getTopCoins().map(CoinDto::toDomain))
        } catch (e: IOException) {
            Resource.Failure(e.message ?: NETWORK_ERROR)
        } catch (e: HttpException) {
            e.toFailure()
        }
    }

    override suspend fun getCoinDetail(coinId: String): Resource<CoinDetail> {
        return try {
            Resource.Success(remoteDataSource.getCoinDetail(coinId).toDomain())
        } catch (e: IOException) {
            Resource.Failure(e.message ?: NETWORK_ERROR)
        } catch (e: HttpException) {
            when (e.code()) {
                HttpURLConnection.HTTP_NOT_FOUND -> Resource.Failure(COIN_NOT_FOUND_ERROR)
                else -> e.toFailure()
            }
        }
    }

    /**
     * A 429 gets its own message on purpose: the free tier counts failed calls toward the limit,
     * so a user who reads "server error" and retries immediately only makes it worse.
     */
    private fun HttpException.toFailure(): Resource.Failure {
        return when (code()) {
            HTTP_TOO_MANY_REQUESTS -> Resource.Failure(RATE_LIMIT_ERROR)
            else -> Resource.Failure(message ?: SERVER_ERROR)
        }
    }

    private companion object {
        const val HTTP_TOO_MANY_REQUESTS = 429

        const val NETWORK_ERROR = "Network error"
        const val SERVER_ERROR = "Server error"
        const val COIN_NOT_FOUND_ERROR = "Coin not found"
        const val RATE_LIMIT_ERROR = "Too many requests, try again in a minute"
    }
}
