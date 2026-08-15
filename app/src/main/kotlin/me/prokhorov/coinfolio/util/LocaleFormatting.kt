package me.prokhorov.coinfolio.util

import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * One source of the locale number symbols for the whole app, so the quantity field and every
 * price use the same separator. It is read on every call, because the user can change the system
 * language while the app runs.
 */
internal fun localeSymbols(): DecimalFormatSymbols {
    return DecimalFormatSymbols.getInstance(Locale.getDefault(Locale.Category.FORMAT))
}

/**
 * Swaps the dot for the separator of the user's locale. Every number on screen goes through here,
 * so the prices and the quantity field always look the same. The way back is
 * [QuantityInput.toCanonical].
 *
 * No grouping separator is added.
 */
internal fun String.localizeDecimalSeparator(): String {
    return replace('.', localeSymbols().decimalSeparator)
}
