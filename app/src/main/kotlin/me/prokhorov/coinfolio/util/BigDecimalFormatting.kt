package me.prokhorov.coinfolio.util

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal.formatAsUsd(): String {
    return "$" + setScale(2, RoundingMode.HALF_UP).toPlainString()
}

fun BigDecimal.formatAsPercent(): String {
    val sign = if (signum() >= 0) "+" else ""
    return sign + setScale(2, RoundingMode.HALF_UP).toPlainString() + "%"
}
