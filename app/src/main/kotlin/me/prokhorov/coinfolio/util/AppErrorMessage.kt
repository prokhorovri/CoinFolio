package me.prokhorov.coinfolio.util

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.domain.result.AppError

/**
 * `:data` only says which kind of failure it is. Presentation turns it into a message. The text
 * stays in `strings.xml` so it follows the language of the device.
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
 * For code outside composition. Snackbar effects are collected in a coroutine, and
 * `stringResource` cannot be used there.
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
            AppError.Storage -> R.string.storage_error_text
            AppError.Unexpected -> R.string.unexpected_error_text
        }
    }

/**
 * Only `Server` has a format argument. When this is empty, both functions above skip
 * `String.format`. A string with no placeholders can then contain a `%` without an error.
 */
private val AppError.messageArgs: Array<Any>
    get() = if (this is AppError.Server) arrayOf(code) else emptyArray()
