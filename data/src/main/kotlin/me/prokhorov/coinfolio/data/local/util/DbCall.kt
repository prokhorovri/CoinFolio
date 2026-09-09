package me.prokhorov.coinfolio.data.local.util

import android.database.SQLException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import me.prokhorov.coinfolio.data.util.logFailure
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource

private const val TAG = "DbCall"

/**
 * Turns any failure of a Room write into a [Resource]. So a ViewModel never gets an exception.
 * [asResource] does the same for reads. `ensureActive()` skips cancellation. It happens when the
 * user leaves the screen during a write. That is not a failure.
 */
@Suppress("TooGenericExceptionCaught")
internal suspend fun <T> dbCall(block: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(block())
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        val error = e.toAppError()
        logFailure(tag = TAG, throwable = e, appError = error)
        Resource.Failure(error)
    }
}

/**
 * Wraps a Room query flow, so a failure arrives as a value. There is no cancellation check
 * here, unlike [dbCall]. `catch` is documented to ignore exceptions that cancel the flow.
 * The flow ends after a failure. Room does not emit again once its query is broken.
 */
internal fun <T> Flow<T>.asResource(): Flow<Resource<T>> {
    return map<T, Resource<T>> { Resource.Success(it) }
        .catch { throwable ->
            val error = throwable.toAppError()
            logFailure(tag = TAG, throwable = throwable, appError = error)
            emit(Resource.Failure(error))
        }
}

/**
 * Room throws [SQLException] for storage problems, both a full disk and a broken database file.
 * Anything else here is our own bug, not a storage problem. The receiver is [Throwable] because
 * `catch` gives its action a [Throwable]. [dbCall] still catches only `Exception`, so an `Error`
 * is not caught there.
 */
private fun Throwable.toAppError(): AppError {
    return when (this) {
        is SQLException -> AppError.Storage
        else -> AppError.Unexpected
    }
}
