package me.prokhorov.coinfolio.data.remote.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

/** `HttpURLConnection` predates RFC 6585, so 429 has no constant there. */
private const val HTTP_TOO_MANY_REQUESTS = 429

/**
 * Turns any failure from a remote call into a [Resource], so nothing reaches a ViewModel as an
 * exception. `ensureActive()` excludes cancellation; without it, leaving a screen mid-request
 * shows an error.
 */
internal suspend fun <T> apiCall(block: suspend () -> T): Resource<T> {
    return try {
        Resource.Success(block())
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Resource.Failure(e.toAppError())
    }
}

private fun Exception.toAppError(): AppError {
    return when (this) {
        is IOException -> AppError.Network
        is HttpException -> httpStatusError(code = code())
        else -> AppError.Unexpected
    }
}

/** 429 is split out because failed calls count toward the limit too, retrying makes it worse. */
private fun httpStatusError(code: Int): AppError {
    return when (code) {
        HttpURLConnection.HTTP_NOT_FOUND -> AppError.NotFound
        HTTP_TOO_MANY_REQUESTS -> AppError.RateLimited
        else -> AppError.Server(code)
    }
}
