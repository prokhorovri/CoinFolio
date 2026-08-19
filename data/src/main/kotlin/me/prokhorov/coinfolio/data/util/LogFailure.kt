package me.prokhorov.coinfolio.data.util

import me.prokhorov.coinfolio.domain.result.AppError
import timber.log.Timber

/**
 * Logs a failure before it becomes a result value. This is the last point where the cause still
 * exists, because [AppError] does not carry it.
 *
 * [AppError.Unexpected] means the cause was not recognised, so it is a bug: error level.
 * Everything else is a normal runtime condition, such as no network or a full disk: warning level.
 */
internal fun logFailure(tag: String, throwable: Throwable, appError: AppError) {
    if (appError == AppError.Unexpected) {
        Timber.tag(tag).e(throwable, "Unrecognised cause")
    } else {
        Timber.tag(tag).w(throwable, "Mapped to %s", appError)
    }
}
