package me.prokhorov.coinfolio.domain.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource

/**
 * Keeps the holdings in memory, so a test can save one and read it back the way the real
 * database does. Set [error] to make every call fail.
 */
class FakePortfolioRepository : PortfolioRepository {

    private val holdings = MutableStateFlow<List<Holding>>(emptyList())

    var error: AppError? = null

    override fun getHoldings(): Flow<Resource<List<Holding>>> {
        return holdings.map { list ->
            val failure = error
            if (failure != null) Resource.Failure(failure) else Resource.Success(list)
        }
    }

    override fun getHolding(coinId: String): Flow<Resource<Holding?>> {
        return holdings.map { list ->
            val failure = error
            if (failure == null) {
                Resource.Success(list.firstOrNull { it.coinId == coinId })
            } else {
                Resource.Failure(failure)
            }
        }
    }

    override suspend fun saveHolding(holding: Holding): Resource<Unit> {
        error?.let { return Resource.Failure(it) }

        holdings.update { list ->
            list.filterNot { it.coinId == holding.coinId } + holding
        }
        return Resource.Success(Unit)
    }

    override suspend fun deleteHolding(coinId: String): Resource<Unit> {
        error?.let { return Resource.Failure(it) }

        holdings.update { list -> list.filterNot { it.coinId == coinId } }
        return Resource.Success(Unit)
    }
}
