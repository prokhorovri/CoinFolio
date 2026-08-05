package me.prokhorov.coinfolio.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.domain.result.AppError

/**
 * `:data` only classifies a failure, presentation gives it words. Keeping the copy in
 * `strings.xml` is what lets it follow the device locale.
 */
@Composable
@ReadOnlyComposable
fun AppError.asMessage(): String {
    val args = messageArgs
    return if (args.isEmpty()) {
        stringResource(messageRes)
    } else {
        stringResource(messageRes, *args)
    }
}

/**
 * For call sites outside composition. Snackbar effects are collected in a coroutine, where
 * `stringResource` isn't available.
 */
fun AppError.asMessage(context: Context): String {
    val args = messageArgs
    return if (args.isEmpty()) {
        context.getString(messageRes)
    } else {
        context.getString(messageRes, *args)
    }
}

@get:StringRes
private val AppError.messageRes: Int
    get() {
        return when (this) {
            AppError.Network -> R.string.network_error_text
            AppError.RateLimited -> R.string.rate_limit_error_text
            AppError.NotFound -> R.string.coin_not_found_error_text
            is AppError.Server -> R.string.server_error_text
            AppError.Unexpected -> R.string.unexpected_error_text
        }
    }

/**
 * Only `Server` has a format argument. The overloads skip `String.format` when this is empty, so
 * an unescaped `%` in a placeholder-free string can't throw.
 */
private val AppError.messageArgs: Array<Any>
    get() = if (this is AppError.Server) arrayOf(code) else emptyArray()
