package me.prokhorov.coinfolio.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

private const val SMALL_PRICE_DIGITS = 4
private val SMALL_PRICE_THRESHOLD = BigDecimal("0.01")

private val MILLION = BigDecimal("1000000")
private val BILLION = BigDecimal("1000000000")
private val TRILLION = BigDecimal("1000000000000")

/**
 * Prices of one cent or more use two decimals. A smaller price would print as $0.00 with a
 * fixed scale. So it uses significant digits instead, and a coin like SHIB still shows a
 * useful value.
 */
fun BigDecimal.formatAsUsd(): String {
    val magnitude = abs()
    val amount = when {
        magnitude >= SMALL_PRICE_THRESHOLD || signum() == 0 -> {
            magnitude.setScale(2, RoundingMode.HALF_UP)
        }

        else -> magnitude.round(MathContext(SMALL_PRICE_DIGITS, RoundingMode.HALF_UP))
    }
    return sign() + "$" + amount.toPlainString().localizeDecimalSeparator()
}

/**
 * Coin amounts keep their full precision. Only the extra zeros at the end are removed. This one
 * keeps the dot on purpose, because Room and [BigDecimal] use that form.
 * [QuantityInput.format] builds the text of the field from it.
 */
fun BigDecimal.formatAsQuantity(): String {
    return stripTrailingZeros().toPlainString()
}

/**
 * Zero is shown as "+0.00%" and never as "-0.00%". A change of -0.001 rounds to zero.
 * BigDecimal has no negative zero, so the minus sign cannot come back.
 */
fun BigDecimal.formatAsPercent(): String {
    val rounded = setScale(2, RoundingMode.HALF_UP)
    val sign = if (rounded.signum() >= 0) "+" else ""
    return sign + rounded.toPlainString().localizeDecimalSeparator() + "%"
}

/**
 * Tells if a price change is a gain. It rounds the same way as [formatAsPercent]. Without
 * that, a value of -0.001 would show "+0.00%" in the loss color.
 */
fun BigDecimal.isGain(): Boolean {
    return setScale(2, RoundingMode.HALF_UP).signum() >= 0
}

/** Market caps and supplies can have 13 digits. This makes them short enough for a stat row. */
fun BigDecimal.formatAsCompact(): String {
    val magnitude = abs()
    val amount = when {
        magnitude >= TRILLION -> magnitude.scaleTo(unit = TRILLION, suffix = "T")
        magnitude >= BILLION -> magnitude.scaleTo(unit = BILLION, suffix = "B")
        magnitude >= MILLION -> magnitude.scaleTo(unit = MILLION, suffix = "M")
        else -> magnitude.setScale(2, RoundingMode.HALF_UP).toPlainString()
    }
    return sign() + amount.localizeDecimalSeparator()
}

fun BigDecimal.formatAsCompactUsd(): String {
    return sign() + "$" + abs().formatAsCompact()
}

private fun BigDecimal.scaleTo(unit: BigDecimal, suffix: String): String {
    return divide(unit, 2, RoundingMode.HALF_UP).toPlainString() + suffix
}

/** Separate from the digits so the currency symbol comes after the sign, like "-$1.20". */
private fun BigDecimal.sign(): String = if (signum() < 0) "-" else ""
