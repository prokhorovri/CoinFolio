package me.prokhorov.coinfolio.util

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import java.util.Locale
import org.junit.Rule
import org.junit.Test

/** Number formatting in a locale where the dot is the decimal separator. */
class BigDecimalFormattingTest {

    @get:Rule
    val localeRule = DefaultLocaleRule(Locale.US)

    // formatAsUsd

    @Test
    fun `a price of one cent or more gets two decimals`() {
        val result = BigDecimal("1234.5").formatAsUsd()

        assertThat(result).isEqualTo("$1234.50")
    }

    @Test
    fun `a price is rounded half up`() {
        val result = BigDecimal("1.005").formatAsUsd()

        assertThat(result).isEqualTo("$1.01")
    }

    @Test
    fun `a price below one cent uses four significant digits`() {
        val result = BigDecimal("0.001234567").formatAsUsd()

        assertThat(result).isEqualTo("$0.001235")
    }

    @Test
    fun `a very small price still shows a useful value`() {
        val result = BigDecimal("0.00002567").formatAsUsd()

        assertThat(result).isEqualTo("$0.00002567")
    }

    @Test
    fun `zero is a price of one cent or more, so it gets two decimals`() {
        val result = BigDecimal.ZERO.formatAsUsd()

        assertThat(result).isEqualTo("$0.00")
    }

    @Test
    fun `a negative price puts the sign before the currency symbol`() {
        val result = BigDecimal("-1.2").formatAsUsd()

        assertThat(result).isEqualTo("-$1.20")
    }

    // formatAsQuantity

    @Test
    fun `a quantity drops the trailing zeros`() {
        val result = BigDecimal("1.500").formatAsQuantity()

        assertThat(result).isEqualTo("1.5")
    }

    @Test
    fun `a whole quantity has no decimals`() {
        val result = BigDecimal("2.00").formatAsQuantity()

        assertThat(result).isEqualTo("2")
    }

    @Test
    fun `a quantity keeps its full precision`() {
        val result = BigDecimal("0.000000000000000001").formatAsQuantity()

        assertThat(result).isEqualTo("0.000000000000000001")
    }

    // formatAsPercent

    @Test
    fun `a positive change gets a plus sign`() {
        val result = BigDecimal("1.234").formatAsPercent()

        assertThat(result).isEqualTo("+1.23%")
    }

    @Test
    fun `a negative change keeps its minus sign`() {
        val result = BigDecimal("-1.235").formatAsPercent()

        assertThat(result).isEqualTo("-1.24%")
    }

    @Test
    fun `zero is shown as plus zero`() {
        val result = BigDecimal.ZERO.formatAsPercent()

        assertThat(result).isEqualTo("+0.00%")
    }

    @Test
    fun `a loss that rounds to zero is shown as plus zero`() {
        val result = BigDecimal("-0.001").formatAsPercent()

        assertThat(result).isEqualTo("+0.00%")
    }

    // isGain, which must agree with formatAsPercent

    @Test
    fun `a positive change is a gain`() {
        assertThat(BigDecimal("0.01").isGain()).isTrue()
    }

    @Test
    fun `zero is a gain`() {
        assertThat(BigDecimal.ZERO.isGain()).isTrue()
    }

    @Test
    fun `a loss that rounds to zero is a gain, so the color matches the text`() {
        val value = BigDecimal("-0.001")

        assertThat(value.isGain()).isTrue()
        assertThat(value.formatAsPercent()).isEqualTo("+0.00%")
    }

    @Test
    fun `a loss that survives rounding is not a gain`() {
        val value = BigDecimal("-0.005")

        assertThat(value.isGain()).isFalse()
        assertThat(value.formatAsPercent()).isEqualTo("-0.01%")
    }

    // formatAsCompact

    @Test
    fun `a trillion gets a T suffix`() {
        val result = BigDecimal("2500000000000").formatAsCompact()

        assertThat(result).isEqualTo("2.50T")
    }

    @Test
    fun `a billion gets a B suffix`() {
        val result = BigDecimal("1234567890").formatAsCompact()

        assertThat(result).isEqualTo("1.23B")
    }

    @Test
    fun `a million gets an M suffix`() {
        val result = BigDecimal("1500000").formatAsCompact()

        assertThat(result).isEqualTo("1.50M")
    }

    @Test
    fun `a value under a million keeps two decimals and no suffix`() {
        val result = BigDecimal("999999.555").formatAsCompact()

        assertThat(result).isEqualTo("999999.56")
    }

    @Test
    fun `a negative value keeps its sign`() {
        val result = BigDecimal("-1500000").formatAsCompact()

        assertThat(result).isEqualTo("-1.50M")
    }

    // formatAsCompactUsd

    @Test
    fun `compact usd puts the currency symbol after the sign`() {
        val result = BigDecimal("-1500000").formatAsCompactUsd()

        assertThat(result).isEqualTo("-$1.50M")
    }

    @Test
    fun `compact usd of a positive value has no sign`() {
        val result = BigDecimal("1500000").formatAsCompactUsd()

        assertThat(result).isEqualTo("$1.50M")
    }
}
