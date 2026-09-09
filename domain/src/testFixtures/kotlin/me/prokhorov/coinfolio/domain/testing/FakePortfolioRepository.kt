package me.prokhorov.coinfolio.domain.testing

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.repository.PortfolioRepository
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource

/**
 * Keeps the holdings in memory. A test can save one and read it back, like the real database
 * does. Set [error] to make every call fail. A read failure ends the flow, like the real code
 * in `:data` does with `catch`. A test can check that a caller subscribes again.
 */
class FakePortfolioRepository : PortfolioRepository {

    private val holdings = MutableStateFlow<List<Holding>>(emptyList())

    var error: AppError? = null

    override fun getHoldings(): Flow<Resource<List<Holding>>> {
        return read { list -> list }
    }

    override fun getHolding(coinId: String): Flow<Resource<Holding?>> {
        return read { list -> list.firstOrNull { it.coinId == coinId } }
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

    private fun <T> read(select: (List<Holding>) -> T): Flow<Resource<T>> {
        return holdings
            .map<List<Holding>, Resource<T>> { list ->
                error?.let { throw ReadFailure(it) }
                Resource.Success(select(list))
            }
            .catch { throwable ->
                if (throwable !is ReadFailure) throw throwable
                emit(Resource.Failure(throwable.error))
            }
    }
}

/** Carries the error through the flow, so `catch` can end it the way Room does. */
private class ReadFailure(val error: AppError) : Exception()
