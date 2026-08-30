package me.prokhorov.coinfolio.util

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import java.util.Locale
import org.junit.Rule
import org.junit.Test

/** Only the parts of number formatting that the locale can change. */
class BigDecimalFormattingGermanLocaleTest {

    @get:Rule
    val localeRule = DefaultLocaleRule(Locale.GERMANY)

    @Test
    fun `a price uses the separator of the locale`() {
        val result = BigDecimal("1234.5").formatAsUsd()

        assertThat(result).isEqualTo("$1234,50")
    }

    @Test
    fun `a percent uses the separator of the locale`() {
        val result = BigDecimal("1.23").formatAsPercent()

        assertThat(result).isEqualTo("+1,23%")
    }

    @Test
    fun `a compact value uses the separator of the locale`() {
        val result = BigDecimal("1500000").formatAsCompact()

        assertThat(result).isEqualTo("1,50M")
    }

    // This one is the exception on purpose. It is the canonical form that Room and BigDecimal
    // read, so it must not follow the locale. QuantityInput.format localizes it for the field.
    @Test
    fun `a quantity keeps the dot whatever the locale is`() {
        val result = BigDecimal("1.5").formatAsQuantity()

        assertThat(result).isEqualTo("1.5")
    }
}
