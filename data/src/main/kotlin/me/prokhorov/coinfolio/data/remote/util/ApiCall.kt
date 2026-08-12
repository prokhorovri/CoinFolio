package me.prokhorov.coinfolio.data.remote.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.domain.result.Resource
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection

/** `HttpURLConnection` is older than RFC 6585, so it has no constant for 429. */
private const val HTTP_TOO_MANY_REQUESTS = 429

/**
 * Turns any failure of a remote call into a [Resource], so a ViewModel never gets an exception.
 * `ensureActive()` skips cancellation. Without it, leaving a screen during a request would show
 * an error.
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

/** 429 has its own case because failed calls also count, so a retry makes the limit worse. */
private fun httpStatusError(code: Int): AppError {
    return when (code) {
        HttpURLConnection.HTTP_NOT_FOUND -> AppError.NotFound
        HTTP_TOO_MANY_REQUESTS -> AppError.RateLimited
        else -> AppError.Server(code)
    }
}
