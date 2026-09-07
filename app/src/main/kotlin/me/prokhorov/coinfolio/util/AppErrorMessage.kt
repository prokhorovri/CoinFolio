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
    // Only Server puts a number into its text. The other errors call it with no arguments.
    // Their text is not formatted, so a `%` sign in it is safe.
    return when (this) {
        is AppError.Server -> stringResource(messageRes, code)
        else -> stringResource(messageRes)
    }
}

/**
 * For code outside composition. Snackbar effects are collected in a coroutine, and
 * `stringResource` cannot be used there.
 */
fun AppError.asMessage(context: Context): String {
    return when (this) {
        is AppError.Server -> context.getString(messageRes, code)
        else -> context.getString(messageRes)
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
