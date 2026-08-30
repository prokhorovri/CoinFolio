package me.prokhorov.coinfolio.util

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import java.util.Locale
import org.junit.Rule
import org.junit.Test

/**
 * The same rules in a locale where the comma is the decimal separator and the dot groups digits.
 * This is the half that a machine running in English would never catch.
 */
class QuantityInputGermanLocaleTest {

    @get:Rule
    val localeRule = DefaultLocaleRule(Locale.GERMANY)

    @Test
    fun `the comma is kept as the decimal separator`() {
        val result = QuantityInput.normalize("1,5")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("1,5"))
    }

    @Test
    fun `the dot is dropped as a grouping separator`() {
        val result = QuantityInput.normalize("1.000")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("1000"))
    }

    // Worth knowing: a user who types the English separator gets a number ten times bigger,
    // with no warning. The dot is a grouping separator here, so it is simply removed.
    @Test
    fun `a dot inside a decimal number is dropped too`() {
        val result = QuantityInput.normalize("1.5")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("15"))
    }

    @Test
    fun `a second comma is refused`() {
        val result = QuantityInput.normalize("1,5,5")

        assertThat(result).isEqualTo(QuantityInputResult.InvalidCharacter)
    }

    @Test
    fun `toCanonical turns the comma into a dot`() {
        val result = QuantityInput.toCanonical("1,5")

        assertThat(result).isEqualTo("1.5")
    }

    @Test
    fun `format shows the comma of the locale`() {
        val result = QuantityInput.format(BigDecimal("1.5"))

        assertThat(result).isEqualTo("1,5")
    }
}
