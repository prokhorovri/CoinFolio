package me.prokhorov.coinfolio.data.local.util

import android.database.SQLException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource

/**
 * Turns any failure from a Room write into a [Resource], so nothing reaches a ViewModel as an
 * exception. The read side of the same idea is [asResource]. `ensureActive()` excludes
 * cancellation, which is what leaving a screen mid-write looks like, not a failure.
 */
internal suspend fun <T> dbCall(block: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(block())
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Resource.Failure(e.toAppError())
    }
}

/**
 * Wraps a Room query flow so a failure arrives as a value instead of tearing down the collecting
 * coroutine. No cancellation guard here, unlike [dbCall]: `catch` is documented as transparent to
 * exceptions thrown to cancel the flow. The flow ends after a failure, since Room does not re-emit
 * once its query breaks.
 */
internal fun <T> Flow<T>.asResource(): Flow<Resource<T>> {
    return map<T, Resource<T>> { Resource.Success(it) }
        .catch { throwable ->
            emit(Resource.Failure(throwable.toAppError()))
        }
}

/**
 * [SQLException] is the umbrella Room throws under, covering a full disk and a corrupt database
 * alike. Anything else reaching here is a bug on our side rather than a storage problem. The
 * receiver is [Throwable] because that is what `catch` hands its action; [dbCall] still catches
 * only `Exception`, so an `Error` keeps propagating.
 */
private fun Throwable.toAppError(): AppError {
    return when (this) {
        is SQLException -> AppError.Storage
        else -> AppError.Unexpected
    }
}
