package me.prokhorov.coinfolio.util

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Two decimals for anything from a cent up. Below that a fixed scale would print $0.00, so small
 * prices switch to significant digits instead and coins like SHIB still show a real number.
 */
fun BigDecimal.formatAsUsd(): String {
    val magnitude = abs()
    val amount = when {
        magnitude >= SMALL_PRICE_THRESHOLD || signum() == 0 -> {
            magnitude.setScale(2, RoundingMode.HALF_UP)
        }

        else -> magnitude.round(MathContext(SMALL_PRICE_DIGITS, RoundingMode.HALF_UP))
    }
    return sign() + "$" + amount.toPlainString()
}

/** Coin amounts keep their full precision — only the padding zeros go. */
fun BigDecimal.formatAsQuantity(): String {
    return stripTrailingZeros().toPlainString()
}

/**
 * Zero reads as "+0.00%" rather than "-0.00%": a change of -0.001 rounds to zero, and BigDecimal
 * has no negative zero to give it a minus sign back.
 */
fun BigDecimal.formatAsPercent(): String {
    val rounded = setScale(2, RoundingMode.HALF_UP)
    val sign = if (rounded.signum() >= 0) "+" else ""
    return sign + rounded.toPlainString() + "%"
}

/**
 * Whether a price change should read as a gain, using the same rounding [formatAsPercent] applies,
 * otherwise a value of -0.001 renders "+0.00%" in the loss color.
 */
fun BigDecimal.isGain(): Boolean {
    return setScale(2, RoundingMode.HALF_UP).signum() >= 0
}

/** Market caps and supplies run to 13 digits; shortened so they stay readable in a stat row. */
fun BigDecimal.formatAsCompact(): String {
    val magnitude = abs()
    val amount = when {
        magnitude >= TRILLION -> magnitude.scaleTo(unit = TRILLION, suffix = "T")
        magnitude >= BILLION -> magnitude.scaleTo(unit = BILLION, suffix = "B")
        magnitude >= MILLION -> magnitude.scaleTo(unit = MILLION, suffix = "M")
        else -> magnitude.setScale(2, RoundingMode.HALF_UP).toPlainString()
    }
    return sign() + amount
}

fun BigDecimal.formatAsCompactUsd(): String {
    return sign() + "$" + abs().formatAsCompact()
}

private fun BigDecimal.scaleTo(unit: BigDecimal, suffix: String): String {
    return divide(unit, 2, RoundingMode.HALF_UP).toPlainString() + suffix
}

/** Kept separate from the digits so the currency symbol lands inside the sign, as "-$1.20". */
private fun BigDecimal.sign(): String = if (signum() < 0) "-" else ""

private const val SMALL_PRICE_DIGITS = 4
private val SMALL_PRICE_THRESHOLD = BigDecimal("0.01")

private val MILLION = BigDecimal("1000000")
private val BILLION = BigDecimal("1000000000")
private val TRILLION = BigDecimal("1000000000000")
